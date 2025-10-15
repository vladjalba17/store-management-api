package com.store.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_sku", columnNames = "sku"),
                @UniqueConstraint(name = "uk_product_name", columnNames = "product_name")})
public class Product {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Setter(AccessLevel.NONE)
    private Long id;

    @Version
    @Setter(AccessLevel.NONE)
    private Long version;

    @Column(nullable = false)
    private String sku;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_description")
    private String productDescription;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;

}
