package br.com.eduardoenemark.pjrw.app.server.resource;

import br.com.eduardoenemark.pjrw.app.server.operation.annotation.ReadOperation;
import br.com.eduardoenemark.pjrw.app.server.operation.annotation.WriteOperation;
import br.com.eduardoenemark.pjrw.app.server.entity.Product;
import br.com.eduardoenemark.pjrw.app.server.service.ProductService;
import com.github.javafaker.Faker;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductResource {

    ProductService service;

    @Autowired
    public ProductResource(ProductService service) {
        this.service = service;
    }

    @WriteOperation
    @PostMapping("/product")
    public ResponseEntity<Product> save(@RequestBody Product product) {
        product.setId(null);
        Product savedProduct = service.save(product);
        return ResponseEntity.ok(savedProduct);
    }

    @WriteOperation
    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @WriteOperation
    @DeleteMapping("/products")
    public ResponseEntity<Void> deleteAll() {
        service.deleteAll();
        return ResponseEntity.ok().build();
    }

    @ReadOperation
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        Product product = service.findById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @ReadOperation
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = service.findAll();
        return ResponseEntity.ok(products);
    }

    @ReadOperation
    @GetMapping("/products/count")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(service.count());
    }

    @GetMapping("/product/generate")
    public ResponseEntity<Product> generate() {
        return ResponseEntity.ok(generateFakeProduct());
    }

    @WriteOperation
    @PostMapping("/product/generate-and-save")
    public ResponseEntity<Product> generateAndSave() {
        val saved = this.service.save(generateFakeProduct());
        return ResponseEntity.ok(saved);
    }

    protected Product generateFakeProduct() {
        val faker = new Faker();
        return new Product()
                .setId(null)
                .setName(faker.commerce().productName())
                .setPrice(BigDecimal.valueOf(faker.number().randomDouble(2, 1, 100)))
                .setAmount(faker.number().numberBetween(1, 1000))
                .setCountry(faker.address().countryCode())
                .setUniversalProductCode(faker.code().ean8())
                .setEntryDate(LocalDate.now())
                .setProducer(faker.company().name());
    }
}
