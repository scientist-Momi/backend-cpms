package dev.olaxomi.backend.request;

import dev.olaxomi.backend.enums.CustomerType;
import dev.olaxomi.backend.enums.Status;
import lombok.Data;

@Data
public class NewCustomerRequest {
    private String name;
    private String alias;
    private String email;
    private String phone;
    private String address;
    private CustomerType customerType;
    private String customerNotes;
}
