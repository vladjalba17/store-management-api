package com.store.management.dto;

import com.store.management.dto.groups.OnCreateProduct;
import com.store.management.dto.groups.OnPriceUpdate;
import com.store.management.dto.groups.OnStockUpdate;
import com.store.management.dto.groups.OnUpdateProduct;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductDto(@Pattern(regexp = "^[A-Z0-9-]{1,64}$", groups = OnCreateProduct.class)
                         String sku,
                         @NotBlank(groups = {OnCreateProduct.class})
                         String productName,
                         String productDescription,
                         @PositiveOrZero(groups = {OnCreateProduct.class, OnUpdateProduct.class, OnPriceUpdate.class})
                         @NotNull(groups = OnPriceUpdate.class)
                         BigDecimal price,
                         @Min(value = 0, groups = {OnCreateProduct.class, OnUpdateProduct.class, OnStockUpdate.class})
                         @NotNull(groups = OnStockUpdate.class)
                         Integer stock,
                         @Null(groups = {OnCreateProduct.class, OnUpdateProduct.class, OnPriceUpdate.class, OnStockUpdate.class})
                         OffsetDateTime createdAt,
                         @Null(groups = OnCreateProduct.class)
                         @NotNull(groups = OnUpdateProduct.class)
                         Boolean active) {
}
