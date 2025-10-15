package br.com.eduardoenemark.pjrw.app.server.resource;

import br.com.eduardoenemark.pjrw.app.server.AppServerApplication;
import br.com.eduardoenemark.pjrw.app.server.config.BaseTestConfiguration;
import br.com.eduardoenemark.pjrw.app.server.entity.Product;
import br.com.eduardoenemark.pjrw.app.server.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {AppServerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ProductResourceTest extends BaseTestConfiguration {

    @Autowired
    RestTemplate restTemplate;

    @Value("${server.port}")
    Integer port;

    @Autowired
    ProductService service;

    @Autowired
    @Qualifier("writeTransactionTemplate")
    TransactionTemplate writeTransactionTemplate;

    String getBaseUrl() {
        return "http://localhost:" + port;
    }

    static final Map<Integer, Object> products = new HashMap<>();

    @Test
    @Order(0)
    void testSaveProduct() {
        // Given
        Product product = new Product()
                .setName("Test Product")
                .setPrice(BigDecimal.valueOf(99.99))
                .setAmount(10)
                .setCountry("BR")
                .setUniversalProductCode("123456789012")
                .setEntryDate(LocalDate.now())
                .setProducer("Test Producer");

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Product> entity = new HttpEntity<>(product, headers);

        val response = restTemplate.postForEntity(getBaseUrl() + "/product", entity, Product.class);
        val savedProduct = response.getBody();
        log.info("Add saved product: {}", savedProduct);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(savedProduct);
        assertNotNull(savedProduct.getId());
        assertEquals(product.getName(), savedProduct.getName());
        assertEquals(product.getPrice(), savedProduct.getPrice());
        assertEquals(product.getAmount(), savedProduct.getAmount());

        // End
        products.put(savedProduct.getId(), savedProduct);
    }

    @Test
    @Order(1)
    void testGetProductById() {
        // Given
        final int savedProductId = 1;
        final Product savedProduct = (Product) products.get(savedProductId);

        // When
        ResponseEntity<Product> response = restTemplate.getForEntity(getBaseUrl() + "/product/" + savedProductId, Product.class);
        val product = response.getBody();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("Response object body: {}", product);
        assertNotNull(product);
        assertEquals(savedProduct.getId(), product.getId());
        assertEquals(savedProduct.getName(), product.getName());
    }

    @Test
    @Order(2)
    void testGetAllProducts() {
        // Given
        saveTestProduct();
        saveTestProduct();

        // When
        val response = restTemplate.getForEntity(getBaseUrl() + "/products", List.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 2);
    }

    @Test
    @Order(3)
    void testCountProducts() {
        // Given
        saveTestProduct();

        // When
        ResponseEntity<Integer> response = restTemplate.getForEntity(getBaseUrl() + "/products/count", Integer.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() >= 1);
    }

    @Test
    @Order(4)
    void testDeleteProduct() {
        // Given
        Product savedProduct = saveTestProduct();
        val url = getBaseUrl() + "/product/" + savedProduct.getId();

        // When
        restTemplate.delete(url);

        // Then
        val response = restTemplate.exchange(URI.create(url), HttpMethod.GET, HttpEntity.EMPTY, Product.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(5)
    void testDeleteAllProducts() {
        // Given
        saveTestProduct();
        val url = getBaseUrl() + "/products";

        // When
        restTemplate.delete(url);

        // Then
        ResponseEntity<Integer> response = restTemplate.getForEntity(url + "/count", Integer.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody());
    }

    @Test
    @Order(6)
    void testGenerateProduct() {
        // When
        ResponseEntity<Product> response = restTemplate.getForEntity(getBaseUrl() + "/product/generate", Product.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getName());
        assertNotNull(response.getBody().getPrice());
    }

    private Product saveTestProduct() {
        Product product = new Product()
                .setName("Test Product")
                .setPrice(BigDecimal.valueOf(99.99))
                .setAmount(10)
                .setCountry("BR")
                .setUniversalProductCode("123456789012")
                .setEntryDate(LocalDate.now())
                .setProducer("Test Producer");

        writeTransactionTemplate.executeWithoutResult(status -> {
            service.save(product);
            status.flush();
        });
        return product;
    }
}