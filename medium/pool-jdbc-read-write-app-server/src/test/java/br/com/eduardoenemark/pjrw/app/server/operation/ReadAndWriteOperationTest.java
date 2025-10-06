package br.com.eduardoenemark.pjrw.app.server.operation;

import br.com.eduardoenemark.pjrw.app.server.AppServerApplication;
import br.com.eduardoenemark.pjrw.app.server.config.routing.RoutingTransaction;
import br.com.eduardoenemark.pjrw.app.server.entity.Product;
import br.com.eduardoenemark.pjrw.app.server.service.ProductService;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;


@Transactional
@Rollback(false)
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = AppServerApplication.class,
        properties = {"logging.level.br.com.eduardoenemark.pjrw.app.server=DEBUG"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class ReadAndWriteOperationTest {

    public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ReadAndWriteOperationTest.class.getName());

    @Autowired
    ProductService productService;

    @Autowired
    @Qualifier("writeTransactionTemplate")
    TransactionTemplate writeTransactionTemplate;

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("123456")
            .withInitScript("ddl-dml-init.sql");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("datasource.url", POSTGRES::getJdbcUrl);
        registry.add("datasource.username", POSTGRES::getUsername);
        registry.add("datasource.password", POSTGRES::getPassword);
    }

    @BeforeAll
    public static void beforeAll() {
        //Start Postgres container.
        POSTGRES.start();
    }

    @AfterAll
    public static void afterAll() {
        POSTGRES.stop();
    }

    @Test
    @Order(0)
    public void countEqualToZero() {
        readOperationInNewThread(() -> {
            val count = productService.count();
            assertEquals(0, count);
        });
    }

    @Test
    @Order(1)
    public void insertProduct() {
        writeOperationInNewThread(() -> {
            val saved = productService.save(ProductService.fakeProduct());
            assertEquals(1, saved.getId());
        });
    }

    @Test
    @Order(2)
    public void insertAndCountProduct() {
        writeOperationInNewThread(() -> {
            val saved = productService.save(ProductService.fakeProduct());
            Assertions.assertTrue(saved.getId() > 1);
            val count = productService.count();
            assertEquals(2, count);
        });
    }

    @Test
    @Order(3)
    public void writeInReadOperation() {
        readOperationInNewThread(() -> {
            Assertions.assertThrows(
                    Exception.class,
                    () -> productService.save(ProductService.fakeProduct()));
        });
    }

    @Test
    @Order(4)
    public void commitInTransactionTemplateAndRollback() {
        writeOperationInNewThread(() -> {
            val p1 = productService.findById(1);
            p1.setName("Name01");
            productService.save(p1);

            writeTransactionTemplate.executeWithoutResult(status -> {
                p1.setName("Name02");
                productService.save(p1);
            });
            throw new UnexpectedRollbackException("Commit between operations");
        });
        val p1 = productService.findById(1);
        assertEquals("Name02", p1.getName());
    }

    @Test
    @Order(5)
    public void operationsBetweenTransactionTemplate() {
        val productId = new AtomicInteger();
        writeTransactionTemplate.executeWithoutResult(status -> {
            LOGGER.info("Saving product");
            val saved = productService.save(ProductService.fakeProduct());
            productId.set(saved.getId());
        });
        assertTrue(productId.get() > 0);

        AtomicBoolean exists = new AtomicBoolean(false);
        writeTransactionTemplate.executeWithoutResult(status -> {
            val product = productService.findById(productId.get());
            LOGGER.info("Finding product with id {}: {}", productId.get(), product);
            exists.set(product != null);
        });
        assertTrue(exists.get());

        writeTransactionTemplate.execute(status -> {
            LOGGER.info("Deleting product with id {}", productId.get());
            productService.delete(productId.get());
            return null;
        });

        LOGGER.info("Checking if product with id {} exists", productId.get());
        val productRef = new AtomicReference<Product>();
        readOperationInNewThread(() -> {
            productRef.set(productService.findById(productId.get()));
            LOGGER.info("Product with id {} exists: {}", productId.get(), productRef.get() != null);
        });
        assertNull(productRef.get());
    }

    @SneakyThrows
    private void readOperationInNewThread(Runnable runnable) {
        val task = new Thread(() -> {
            try {
                RoutingTransaction.readBindResources();
                runnable.run();
            } finally {
                RoutingTransaction.readUnbindResources();
            }
        });
        task.start();
        task.join();
    }

    @SneakyThrows
    private void writeOperationInNewThread(Runnable runnable) {
        val task = new Thread(() -> {
            try {
                RoutingTransaction.writeBindResources();
                runnable.run();
            } finally {
                RoutingTransaction.writeUnbindResources();
            }
        });
        task.start();
        task.join();
    }
}
