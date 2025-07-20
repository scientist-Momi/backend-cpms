package dev.olaxomi.backend.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class NewCustomerTransactionRequest {
    private UUID customerId;
    private List<NewCustomerTransactionDetailRequest> transactionDetails;
}
