package dev.olaxomi.backend.dto;

import dev.olaxomi.backend.enums.CustomerType;
import dev.olaxomi.backend.enums.Status;
import dev.olaxomi.backend.model.CustomerWallet;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CustomerDto {
    private UUID customerId;
    private String name;
    private String alias;
    private String email;
    private String phone;
    private String address;
    private Status status;
    private CustomerType customerType;
    private BigDecimal creditLimit;
    private String customerNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CustomerWallet customerWallet;
}
