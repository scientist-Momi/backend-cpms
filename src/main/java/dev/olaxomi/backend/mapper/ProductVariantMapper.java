package dev.olaxomi.backend.mapper;

import dev.olaxomi.backend.dto.ProductVariantDto;
import dev.olaxomi.backend.dto.UserDto;
import dev.olaxomi.backend.model.ProductVariant;
import dev.olaxomi.backend.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductVariantMapper {
    @Autowired
    private ModelMapper modelMapper;

    public ProductVariantDto toDto(ProductVariant variant){
        return modelMapper.map(variant, ProductVariantDto.class);
    }

    public List<ProductVariantDto> toDtoList(List<ProductVariant> variants) {
        return variants.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    public ProductVariant fromDto(ProductVariantDto variantDto) {
        return modelMapper.map(variantDto, ProductVariant.class);
    }
}
