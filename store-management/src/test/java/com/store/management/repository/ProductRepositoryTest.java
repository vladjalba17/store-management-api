package com.store.management.repository;

import com.store.management.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product p1, p2, p3;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        p1 = newProduct("SKU-001", "Mouse", true, "D1", new BigDecimal("10.00"), 5);
        p2 = newProduct("SKU-002", "Laptop", false, "D2", new BigDecimal("7"), 3);
        p3 = newProduct("SKU-003", "Tv", true, "D3", new BigDecimal("123"), 9);

        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);
    }

    @Test
    void findBySku_found() {
        Optional<Product> found = productRepository.findBySku("SKU-001");
        assertThat(found).isPresent();
        assertThat(found.get().getProductName()).isEqualTo("Mouse");
    }

    @Test
    void findBySku_notFound() {
        assertThat(productRepository.findBySku("SKU-999")).isNotPresent();
    }

    @Test
    void existsByFields_checks() {
        assertThat(productRepository.existsBySku("SKU-003")).isTrue();
        assertThat(productRepository.existsByProductName("Tv")).isTrue();
    }

    @Test
    void findAllByActive_paged() {
        Page<Product> active = productRepository.findAllByActive(true, PageRequest.of(0, 10));
        assertThat(active.getTotalElements()).isEqualTo(2);
        assertThat(active.getContent())
                .extracting(Product::getSku)
                .containsExactlyInAnyOrder("SKU-001", "SKU-003");

        Page<Product> inactive = productRepository.findAllByActive(false, PageRequest.of(0, 10));
        assertThat(inactive.getTotalElements()).isEqualTo(1);
        assertThat(inactive.getContent().get(0).getSku()).isEqualTo("SKU-002");
    }

    private Product newProduct(String sku, String name, boolean active,
                               String desc, BigDecimal price, int stock) {
        Product p = new Product();
        p.setSku(sku);
        p.setProductName(name);
        p.setProductDescription(desc);
        p.setPrice(price);
        p.setStock(stock);
        p.setActive(active);
        return p;
    }
}
