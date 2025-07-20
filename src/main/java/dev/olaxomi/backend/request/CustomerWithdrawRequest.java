package dev.olaxomi.backend.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CustomerWithdrawRequest {
    private UUID customerId;
    @Positive(message = "Withdrawal amount must be positive")
    private BigDecimal amount;
    private String note;
}
