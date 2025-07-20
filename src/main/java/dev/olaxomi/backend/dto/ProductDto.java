package dev.olaxomi.backend.dto;

import dev.olaxomi.backend.model.ProductPrice;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.Data;
import java.util.*;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String brand;
    private int inventory;
    private int quantitySold;
    private String description;
    private List<ProductPrice> prices = new ArrayList<>();

    public ProductPrice getLatestPrice() {
        return prices.stream()
                .max(Comparator.comparing(ProductPrice::getCreatedAt))
                .orElse(null);
    }
}

