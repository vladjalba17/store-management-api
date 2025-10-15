package com.store.management.mapper;

import com.store.management.dto.ProductDto;
import com.store.management.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Product toEntity(ProductDto productDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget Product target, ProductDto src);

    ProductDto toDto(Product product);
}
