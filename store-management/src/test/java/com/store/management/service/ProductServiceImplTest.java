package com.store.management.service;

import com.store.management.dto.ProductDto;
import com.store.management.entity.Product;
import com.store.management.exception.FieldConflictException;
import com.store.management.exception.ProductAlreadyExistsException;
import com.store.management.exception.ResourceNotFoundException;
import com.store.management.mapper.ProductMapper;
import com.store.management.repository.ProductRepository;
import com.store.management.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    ProductRepository productRepository;
    @Spy
    private ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @InjectMocks
    ProductServiceImpl service;

    @Captor
    ArgumentCaptor<Product> productCaptor;

    private final ProductDto baseDto = dto("SKU-1", "Prod", "D", "10.00", 5, true);
    private final Product baseEntity = entity("SKU-1", "Prod", "D", "10.00", 5, true);

    @Test
    void createProduct_ok() {
        when(productRepository.existsBySku("SKU-1")).thenReturn(false);
        when(productRepository.existsByProductName("Prod")).thenReturn(false);
        when(mapper.toEntity(baseDto)).thenReturn(baseEntity);

        service.createProduct(baseDto);

        verify(productRepository).save(productCaptor.capture());

        var saved = productCaptor.getValue();

        assertThat(saved.getSku()).isEqualTo("SKU-1");
        assertThat(saved.getProductName()).isEqualTo("Prod");
        assertThat(saved.getProductDescription()).isEqualTo("D");
        assertThat(saved.getPrice()).isEqualByComparingTo("10.00");
        assertThat(saved.getStock()).isEqualTo(5);
        assertThat(saved.getActive()).isTrue();
    }

    @Test
    void createProduct_conflict_bothFields() {
        when(productRepository.existsBySku("SKU-1")).thenReturn(true);
        when(productRepository.existsByProductName("Prod")).thenReturn(true);

        assertThatThrownBy(() -> service.createProduct(baseDto))
                .isInstanceOf(FieldConflictException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_conflict_skuOnly() {
        when(productRepository.existsBySku("SKU-1")).thenReturn(true);
        when(productRepository.existsByProductName("Prod")).thenReturn(false);

        assertThatThrownBy(() -> service.createProduct(baseDto))
                .isInstanceOf(ProductAlreadyExistsException.class);
    }

    @Test
    void updateProduct_ok() {
        var existing = entity("SKU-1", "Old", "OD", "1.00", 1, true);
        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.of(existing));

        service.updateProduct("SKU-1", baseDto);

        verify(productRepository).save(productCaptor.capture());
        var saved = productCaptor.getValue();

        assertThat(saved).isSameAs(existing);
        assertThat(saved.getSku()).isEqualTo("SKU-1");
        assertThat(saved.getProductName()).isEqualTo("Prod");
        assertThat(saved.getProductDescription()).isEqualTo("D");
        assertThat(saved.getPrice()).isEqualByComparingTo("10.00");
        assertThat(saved.getStock()).isEqualTo(5);
        assertThat(saved.getActive()).isTrue();
    }

    @Test
    void updateProduct_throws_notFound() {
        when(productRepository.findBySku("SKU1000")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateProduct("SKU1000", baseDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProduct_throws_optimisticLock() {
        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.of(baseEntity));
        doThrow(new OptimisticLockingFailureException("")).when(productRepository).save(baseEntity);

        assertThatThrownBy(() -> service.updateProduct("SKU-1", baseDto))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void updatePrice_ok() {
        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.of(baseEntity));

        service.updateProductPrice("SKU-1", new BigDecimal("15.50"));

        verify(productRepository).save(productCaptor.capture());
        var saved = productCaptor.getValue();

        assertThat(saved).isSameAs(baseEntity);
        assertThat(saved.getPrice()).isEqualByComparingTo("15.50");
        assertThat(saved.getSku()).isEqualTo("SKU-1");
    }

    @Test
    void updatePrice_notFound_throws() {
        when(productRepository.findBySku("SKU1000")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateProductPrice("SKU1000", new BigDecimal("1")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStock_ok_setsAndSaves_capturedFields() {
        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.of(baseEntity));

        service.updateProductStock("SKU-1", 100);

        verify(productRepository).save(productCaptor.capture());
        var saved = productCaptor.getValue();

        assertThat(saved).isSameAs(baseEntity);
        assertThat(saved.getStock()).isEqualTo(100);
        assertThat(saved.getSku()).isEqualTo("SKU-1");
    }

    @Test
    void findBySku_ok() {
        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.of(baseEntity));
        when(mapper.toDto(baseEntity)).thenReturn(baseDto);

        var dto = service.findBySku("SKU-1");

        assertThat(dto.sku()).isEqualTo("SKU-1");
        verify(mapper).toDto(baseEntity);
    }

    @Test
    void findBySku_notFound_throws() {
        when(productRepository.findBySku("SKU1000")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findBySku("SKU1000"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProducts_ok() {
        var p1 = entity("SKU-1", "aa", "", "1.00", 1, true);
        var p2 = entity("SKU-2", "b", "", "2", 2, true);
        var page = new PageImpl<>(List.of(p1, p2), PageRequest.of(0, 10), 2);

        when(productRepository.findAllByActive(true, PageRequest.of(0, 10))).thenReturn(page);
        when(mapper.toDto(p1)).thenReturn(dto("SKU-1", "aa", "", "1.00", 1, true));
        when(mapper.toDto(p2)).thenReturn(dto("SKU-2", "b", "", "2", 2, true));

        Page<ProductDto> result = service.getProducts(true, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.map(ProductDto::sku).getContent()).containsExactly("SKU-1", "SKU-2");
    }

    @Test
    void deleteProduct_ok() {
        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.of(baseEntity));

        service.deleteProduct("SKU-1");

        verify(productRepository).delete(productCaptor.capture());
        var deleted = productCaptor.getValue();
        assertThat(deleted.getSku()).isEqualTo("SKU-1");
    }

    private ProductDto dto(String sku, String name, String desc,
                           String price, int stock, Boolean active) {
        return new ProductDto(sku, name, desc, bd(price), stock, null, active);
    }

    private Product entity(String sku, String name, String desc,
                           String price, int stock, Boolean active) {
        var e = new Product();
        e.setSku(sku);
        e.setProductName(name);
        e.setProductDescription(desc);
        e.setPrice(bd(price));
        e.setStock(stock);
        e.setActive(active);
        return e;
    }

    private static BigDecimal bd(String v) {
        return v == null ? null : new BigDecimal(v);
    }
}
