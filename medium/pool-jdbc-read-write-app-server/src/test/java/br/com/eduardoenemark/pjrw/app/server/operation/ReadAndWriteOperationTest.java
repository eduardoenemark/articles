package br.com.eduardoenemark.pjrw.app.server.operation;

import br.com.eduardoenemark.pjrw.app.server.AppServerApplication;
import br.com.eduardoenemark.pjrw.app.server.config.BaseTestConfiguration;
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
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;


@Transactional
@Rollback(false)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = AppServerApplication.class,
        properties = {"logging.level.br.com.eduardoenemark.pjrw.app.server=DEBUG"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class ReadAndWriteOperationTest extends BaseTestConfiguration {

    public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ReadAndWriteOperationTest.class.getName());

    @Autowired
    ProductService productService;

    @Autowired
    @Qualifier("writeTransactionTemplate")
    TransactionTemplate writeTransactionTemplate;

    @Autowired
    ApplicationContext context;

    @Test
    @Order(0)
    public void countEqualToZero() {
        executeReadOperation(() -> {
            val count = productService.count();
            assertEquals(0, count);
        });
    }

    @Test
    @Order(1)
    public void insertProduct() {
        executeWriteOperation(() -> {
            val saved = productService.save(ProductService.fakeProduct());
            assertEquals(1, saved.getId());
        });
    }

    @Test
    @Order(2)
    public void insertAndCountProduct() {
        executeWriteOperation(() -> {
            val saved = productService.save(ProductService.fakeProduct());
            Assertions.assertTrue(saved.getId() > 1);
            val count = productService.count();
            assertEquals(2, count);
        });
    }

    @Test
    @Order(3)
    public void writeInReadOperation() {
        executeReadOperation(() -> {
            Assertions.assertThrows(
                    Exception.class,
                    () -> productService.save(ProductService.fakeProduct()));
        });
    }

    @Test
    @Order(4)
    public void commitInTransactionTemplateAndRollback() {
        executeWriteOperation(() -> {
            val p1 = productService.findById(1);
            p1.setName("Name01");
            productService.save(p1);

            writeTransactionTemplate.executeWithoutResult(status -> {
                p1.setName("Name02");
                productService.save(p1);
                status.flush();
            });
            throw new UnexpectedRollbackException("Commit between operations");
        });
        val p1 = productService.findById(1);
        assertEquals("Name02", p1.getName());
    }

    AtomicInteger productId = new AtomicInteger();

    @SneakyThrows
    @Test
    @Order(5)
    public void operationsBetweenTransactionTemplate() {
        val t1 = context.getBean("writeTransactionTemplate", TransactionTemplate.class);
        val t2 = context.getBean("writeTransactionTemplate", TransactionTemplate.class);

        t1.executeWithoutResult(status -> {
            LOGGER.info("Saving product");
            val saved = productService.save(ProductService.fakeProduct());
            productId.set(saved.getId());
            status.flush();
        });
        assertTrue(productId.get() > 0);

        AtomicBoolean exists = new AtomicBoolean(false);
        t2.execute(status -> {
            val product = productService.findById(productId.get());
            LOGGER.info("Finding product with id {}: {}", productId.get(), product);
            exists.set(product != null);
            status.flush();
            return product;
        });
        assertTrue(exists.get());

        val t3 = context.getBean("writeTransactionTemplate", TransactionTemplate.class);
        t3.execute(status -> {
            LOGGER.info("Deleting product with id {}", productId.get());
            productService.deleteById(productId.get());
            status.flush();
            return null;
        });

        LOGGER.info("Checking if product with id {} exists", productId.get());
        val productRef = new AtomicReference<Product>();
        executeReadOperation(() -> {
            productRef.set(productService.findById(productId.get()));
            LOGGER.info("Product with id {} exists: {}", productId.get(), productRef.get() != null);
        });
        assertNull(productRef.get());
    }

    @SneakyThrows
    private void executeReadOperation(Runnable runnable) {
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
    private void executeWriteOperation(Runnable runnable) {
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
