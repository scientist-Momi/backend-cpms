package dev.olaxomi.backend.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CustomerDebtAdjustmentRequest {
    @NotNull
    private UUID customerId;

    @Positive(message = "Debt amount must be positive")
    private BigDecimal amount;

    @NotBlank
    private String note;
}
