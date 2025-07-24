package dev.olaxomi.backend.dto;

import lombok.Data;

@Data
public class ProductVariantDto {
    private Long id;
    private double weight;
    private int inventory;
}
