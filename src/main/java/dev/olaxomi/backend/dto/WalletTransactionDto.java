package dev.olaxomi.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletTransactionDto {
    private Long transactionId;
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction;
    private String transactionType;
    private String reference;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
