package com.store.management.service;

import com.store.management.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    void createProduct(ProductDto productDto);

    void updateProduct(String sku, ProductDto productDto);

    void updateProductPrice(String sku, BigDecimal newPrice);

    void updateProductStock(String sku, Integer stock);

    ProductDto findBySku(String sku);

    Page<ProductDto> getProducts(boolean active, Pageable pageable);

    void deleteProduct(String sku);

}
