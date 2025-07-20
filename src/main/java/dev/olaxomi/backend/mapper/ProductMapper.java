package dev.olaxomi.backend.mapper;

import dev.olaxomi.backend.dto.ProductDto;
import dev.olaxomi.backend.model.Product;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    @Autowired
    private ModelMapper modelMapper;

    public ProductDto toDto(Product product){
        ProductDto dto = modelMapper.map(product, ProductDto.class);
        if (product.getPrices() != null) {
            dto.setPrices(product.getPrices());
        }
        return dto;
    }

    public List<ProductDto> toDtoList(List<Product> products) {
        return products.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    public Product fromDto(ProductDto productDto) {
        return modelMapper.map(productDto, Product.class);
    }
}
