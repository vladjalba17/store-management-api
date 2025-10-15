package com.store.management.controller;

import com.store.management.constants.StoreManagementConstants;
import com.store.management.dto.ProductDto;
import com.store.management.dto.ResponseDto;
import com.store.management.dto.groups.OnCreateProduct;
import com.store.management.dto.groups.OnPriceUpdate;
import com.store.management.dto.groups.OnStockUpdate;
import com.store.management.dto.groups.OnUpdateProduct;
import com.store.management.service.ProductService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ResponseDto> createProduct(@Validated(OnCreateProduct.class) @RequestBody ProductDto productDto) {
        service.createProduct(productDto);
        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(new ResponseDto(StoreManagementConstants.STATUS_201, StoreManagementConstants.PRODUCT_CREATED));
    }

    @PutMapping("/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ResponseDto> updateProduct(@PathVariable @Pattern(regexp = StoreManagementConstants.SKU) String sku,
                                                     @Validated(OnUpdateProduct.class) @RequestBody ProductDto productDto) {
        service.updateProduct(sku, productDto);
        return ResponseEntity.
                status(HttpStatus.OK)
                .body(new ResponseDto(StoreManagementConstants.STATUS_200, StoreManagementConstants.PRODUCT_UPDATED));
    }

    @PatchMapping("/{sku}/price")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ResponseDto> changePrice(@PathVariable @Pattern(regexp = StoreManagementConstants.SKU) String sku,
                                                   @Validated(OnPriceUpdate.class) @RequestBody ProductDto productDto) {
        service.updateProductPrice(sku, productDto.price());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(StoreManagementConstants.STATUS_200, StoreManagementConstants.PRODUCT_PRICE_UPDATED));
    }

    @PatchMapping("/{sku}/stock")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ResponseDto> changeStock(@PathVariable @Pattern(regexp = StoreManagementConstants.SKU) String sku,
                                                   @Validated(OnStockUpdate.class) @RequestBody ProductDto productDto) {
        service.updateProductStock(sku, productDto.stock());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(StoreManagementConstants.STATUS_200, StoreManagementConstants.PRODUCT_STOCK_UPDATED));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<Page<ProductDto>> getProducts(@RequestParam(defaultValue = "true") boolean active,
                                                        @PageableDefault(size = 5, sort = "createdAt",
                                                                direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.
                status(HttpStatus.OK).body(service.getProducts(active, pageable));
    }

    @GetMapping("/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<ProductDto> findBySku(@PathVariable @Pattern(regexp = StoreManagementConstants.SKU) String sku) {
        ProductDto productDto = service.findBySku(sku);
        return ResponseEntity.
                status(HttpStatus.OK).body(productDto);
    }

    @DeleteMapping("/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ResponseDto> deleteProduct(@PathVariable @Pattern(regexp = StoreManagementConstants.SKU) String sku) {
        service.deleteProduct(sku);
        return ResponseEntity.
                status(HttpStatus.OK)
                .body(new ResponseDto(StoreManagementConstants.STATUS_200, StoreManagementConstants.PRODUCT_DELETED));
    }

}
