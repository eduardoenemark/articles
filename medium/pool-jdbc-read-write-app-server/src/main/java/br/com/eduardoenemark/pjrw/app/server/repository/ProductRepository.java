package br.com.eduardoenemark.pjrw.app.server.repository;

import br.com.eduardoenemark.pjrw.app.server.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByProducer(String producer);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
    List<Product> findByName(String name);

    @Query("SELECT COUNT(p) FROM Product p")
    long count();
}

