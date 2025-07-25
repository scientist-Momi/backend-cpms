package dev.olaxomi.backend.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NewCustomerTransactionDetailRequest {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineDiscount;
}
