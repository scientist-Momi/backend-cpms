package dev.olaxomi.backend.request;

import dev.olaxomi.backend.enums.CustomerType;
import dev.olaxomi.backend.enums.Status;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateCustomerRequest {
    private String name;
    private String alias;
    private String email;
    private String phone;
    private String address;
    private Status status;
    private CustomerType customerType;
    private BigDecimal creditLimit;
    private String customerNotes;
}
