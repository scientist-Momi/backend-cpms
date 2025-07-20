package dev.olaxomi.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CustomerTransactionDto {
    private Long transactionId;
    private UUID customerId;
    private BigDecimal totalAmount;
    private Integer totalQuantity;
    private BigDecimal totalDiscount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CustomerTransactionDetailDto> transactionDetails;
}
