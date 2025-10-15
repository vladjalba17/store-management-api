package com.store.management.repository;

import com.store.management.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    boolean existsByProductName(String productName);

    boolean existsBySku(String sku);

    Page<Product> findAllByActive(boolean active, Pageable pageable);
}
