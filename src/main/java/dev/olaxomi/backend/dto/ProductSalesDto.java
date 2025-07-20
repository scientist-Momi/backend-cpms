package dev.olaxomi.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ProductSalesDto {
    private Long productId;
    private String productName;
    private Long salesCount;
}
