package com.store.management.mapper;

import com.store.management.dto.ProductDto;
import com.store.management.entity.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ProductMapperTest {
    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void toEntity_onCreate() {
        var dto = dto("SKU-123", "Mouse", "New 1600DPI mouse", "9.99", 7, null, null);

        var e = mapper.toEntity(dto);

        assertNotNull(e);
        assertEquals("SKU-123", e.getSku());
        assertEquals("Mouse", e.getProductName());
        assertEquals("New 1600DPI mouse", e.getProductDescription());
        assertEquals(new BigDecimal("9.99"), e.getPrice());
        assertEquals(7, e.getStock());
        assertTrue(e.getActive());

        assertNull(e.getCreatedAt());
        assertNull(e.getVersion());
    }

    @Test
    void updateEntity_appliesNonNulls() {
        var existing = entity("SKU-OLD", "Old", "Old D", "1.00", 1, true, "2025-10-01T10:00:00Z");
        var src = dto("SKU-NEW", "NewName", null, "20", 8, "2025-10-15T10:00:00Z", false);

        mapper.updateEntity(existing, src);

        assertEquals("SKU-NEW", existing.getSku());
        assertEquals("NewName", existing.getProductName());
        assertEquals("Old D", existing.getProductDescription());
        assertEquals(new BigDecimal("20"), existing.getPrice());
        assertEquals(8, existing.getStock());
        assertEquals(Boolean.FALSE, existing.getActive());
        assertEquals(OffsetDateTime.parse("2025-10-01T10:00:00Z"), existing.getCreatedAt());
    }

    @Test
    void toDto_mapsAllReadableFields() {
        var e = entity("SKU-1", "Name", "D", "2.34", 3, true, "2025-10-15T10:00:00Z");

        var dto = mapper.toDto(e);

        assertNotNull(dto);
        assertEquals("SKU-1", dto.sku());
        assertEquals("Name", dto.productName());
        assertEquals("D", dto.productDescription());
        assertEquals(new BigDecimal("2.34"), dto.price());
        assertEquals(3, dto.stock());
        assertEquals(Boolean.TRUE, dto.active());
        assertEquals(OffsetDateTime.parse("2025-10-15T10:00:00Z"), dto.createdAt());
    }

    @Test
    void nullInputs_returnNull() {
        assertNull(mapper.toEntity(null));
        assertNull(mapper.toDto(null));

        var e = new Product();
        e.setProductName("Product");
        mapper.updateEntity(e, null);
        assertEquals("Product", e.getProductName());
    }

    private ProductDto dto(String sku, String name, String desc,
                           String price, int stock, String createdAtIso, Boolean active) {
        return new ProductDto(sku, name, desc, bd(price), stock, odt(createdAtIso), active);
    }

    private Product entity(String sku, String name, String desc,
                           String price, int stock, Boolean active, String createdAtIso) {
        var e = new Product();
        e.setSku(sku);
        e.setProductName(name);
        e.setProductDescription(desc);
        e.setPrice(bd(price));
        e.setStock(stock);
        e.setActive(active);
        e.setCreatedAt(odt(createdAtIso));
        return e;
    }

    private static BigDecimal bd(String v) {
        return v == null ? null : new BigDecimal(v);
    }

    private static OffsetDateTime odt(String iso) {
        return iso == null ? null : OffsetDateTime.parse(iso);
    }

}
