package com.marketsystem.api.v1.product.mapper;

import com.marketsystem.api.v1.product.dto.ProductRequestDto;
import com.marketsystem.api.v1.product.dto.ProductResponseDto;
import com.marketsystem.api.v1.product.entity.Product;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    List<Product> toEntityList(List<ProductRequestDto.Save> saveDtos);

    // Update 시 null 필드는 무시
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProductRequestDto.Update updateDto, @MappingTarget Product product);

    ProductResponseDto toDto(Product product);

    List<ProductResponseDto> toDtoList(List<Product> products);
}
