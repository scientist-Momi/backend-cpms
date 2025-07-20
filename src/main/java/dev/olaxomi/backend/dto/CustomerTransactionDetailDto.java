package dev.olaxomi.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerTransactionDetailDto {
    private Long detailId;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineDiscount;
}
