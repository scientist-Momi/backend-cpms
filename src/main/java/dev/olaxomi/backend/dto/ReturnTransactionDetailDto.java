package dev.olaxomi.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReturnTransactionDetailDto {
    private Long detailId;
    private Long productId;
    private Long variantId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineDiscount;
    private ProductVariantDto variant;
    private ProductDto product;
}
