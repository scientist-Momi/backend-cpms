package dev.olaxomi.backend.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NewProductRequest {
    private String name;
    private String brand;
    private int inventory;
    private BigDecimal price;
    private String description;
}
