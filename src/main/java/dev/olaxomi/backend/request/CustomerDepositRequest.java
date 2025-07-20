package dev.olaxomi.backend.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CustomerDepositRequest {
    private UUID customerId;
    private BigDecimal amount;
    private String note;
}
