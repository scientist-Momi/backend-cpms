package dev.olaxomi.backend.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NewProductUnitRequest {
    private BigDecimal weight;
    private int inventory;
}
