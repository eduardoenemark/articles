package br.com.eduardoenemark.pjrw.app.server.service;

import br.com.eduardoenemark.pjrw.app.server.entity.Product;
import br.com.eduardoenemark.pjrw.app.server.repository.ProductRepository;
import com.github.javafaker.Faker;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductService {

    ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Product findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Product> findByName(String name) {
        return repository.findByNameContaining(name);
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Long count() {
        return repository.count();
    }

    @Transactional
    public Product save(Product product) {
        return repository.save(product);
    }

    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    public static Product fakeProduct() {
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

