package dev.olaxomi.backend.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateCustomerTransactionRequest {
    private List<NewCustomerTransactionDetailRequest> transactionDetails;
}
