package dev.olaxomi.backend.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data
public class NewUserRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
}
