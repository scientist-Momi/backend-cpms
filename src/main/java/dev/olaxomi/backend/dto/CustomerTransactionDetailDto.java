package dev.olaxomi.backend.dto;

import dev.olaxomi.backend.model.ProductVariant;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerTransactionDetailDto {
    private Long detailId;
    private Long productId;
    private Long variantId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineDiscount;
    private ProductVariantDto variant;
}
