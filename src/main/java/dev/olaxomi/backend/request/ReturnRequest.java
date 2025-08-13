package dev.olaxomi.backend.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReturnRequest {
    private UUID customerId;
    private Long transactionId;
    private List<ReturnDetailRequest> returnDetails;
}
