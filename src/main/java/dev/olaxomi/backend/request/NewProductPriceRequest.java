package dev.olaxomi.backend.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NewProductPriceRequest {
    private BigDecimal price;
}
