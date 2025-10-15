package com.store.management.service.impl;

import com.store.management.dto.ProductDto;
import com.store.management.exception.FieldConflictException;
import com.store.management.exception.ProductAlreadyExistsException;
import com.store.management.exception.ResourceNotFoundException;
import com.store.management.mapper.ProductMapper;
import com.store.management.repository.ProductRepository;
import com.store.management.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper mapper;


    @Transactional
    @CacheEvict(value = "productBySku", key = "#productDto.sku()")
    public void createProduct(ProductDto productDto) {
        log.debug("createProduct start SKU={}", productDto.sku());
        assertNoDuplicates(productDto);
        productRepository.save(mapper.toEntity(productDto));
        log.info("Product with SKU: " + productDto.sku() + " has been created");
    }

    @Transactional
    @CacheEvict(value = "productBySku", key = "#sku")
    public void updateProduct(String sku, ProductDto productDto) {
        log.debug("updateProduct start SKU={}", sku);
        var existingProduct = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));

        mapper.updateEntity(existingProduct, productDto);
        try {
            productRepository.save(existingProduct);
            log.info("Product with SKU: {} updated successfully", sku);
        } catch (OptimisticLockingFailureException e) {
            throw new OptimisticLockingFailureException(
                    "Product with SKU: " + sku + " could not be updated. Resource was modified concurrently.", e);
        }
    }

    @Transactional
    @CacheEvict(value = "productBySku", key = "#sku")
    public void updateProductPrice(String sku, BigDecimal price) {
        log.debug("updateProductPrice start SKU={}", sku);
        var existingProduct = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
        var old = existingProduct.getPrice();
        existingProduct.setPrice(price);
        try {
            productRepository.save(existingProduct);
            log.info("Price updated successfully for SKU={} old={} new={}", sku, old, price);
        } catch (OptimisticLockingFailureException e) {
            throw new OptimisticLockingFailureException(
                    "Price for product with SKU: " + sku + " could not be updated. Resource was modified concurrently.", e);
        }
    }

    @Transactional
    @CacheEvict(value = "productBySku", key = "#sku")
    public void updateProductStock(String sku, Integer stock) {
        log.debug("updateProductStock start SKU={}", sku);
        var existingProduct = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
        var old = existingProduct.getStock();
        existingProduct.setStock(stock);
        try {
            productRepository.save(existingProduct);
            log.info("Stock updated successfully for SKU={} old={} new={}", sku, old, stock);
        } catch (OptimisticLockingFailureException e) {
            throw new OptimisticLockingFailureException(
                    "Stock for product with SKU: " + sku + " could not be updated. Resource was modified concurrently.", e);
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "productBySku", key = "#sku")
    public ProductDto findBySku(String sku) {
        log.debug("findBySku start SKU={}", sku);
        return productRepository.findBySku(sku)
                .map(p -> {
                    log.info("Product found sku={}", sku);
                    return mapper.toDto(p);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(boolean active, Pageable pageable) {
        log.debug("--- getProducts start ---");
        var page = productRepository.findAllByActive(active, pageable)
                .map(mapper::toDto);
        log.info("Products found active={}: {}", active, page.getTotalElements());
        return page;
    }

    @Transactional
    @CacheEvict(value = "productBySku", key = "#sku")
    public void deleteProduct(String sku) {
        log.debug("deleteProduct start SKU={}", sku);
        var existing = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));

        try {
            productRepository.delete(existing);
            log.info("Product deleted with SKU={}", sku);
        } catch (OptimisticLockingFailureException e) {
            throw new OptimisticLockingFailureException(
                    "Delete for product with SKU: " + sku + " failed. Resource was modified concurrently.", e);
        }
    }

    private void assertNoDuplicates(ProductDto dto) {
        log.debug("assertNoDuplicates start");
        var skuTaken = productRepository.existsBySku(dto.sku());
        var nameTaken = productRepository.existsByProductName(dto.productName());

        if (skuTaken && nameTaken) {
            throw new FieldConflictException(Map.of(
                    "sku", dto.sku() + " already exists",
                    "productName", dto.productName() + " already exists"));
        }
        if (skuTaken) throw new ProductAlreadyExistsException("SKU already exists: " + dto.sku());
        if (nameTaken) throw new ProductAlreadyExistsException("Product name already exists: " + dto.productName());
        log.debug("No constraint issues found for adding new product with SKU: {}", dto.sku());
    }
}
