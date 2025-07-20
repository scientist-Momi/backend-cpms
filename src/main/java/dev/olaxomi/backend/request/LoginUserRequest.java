package dev.olaxomi.backend.request;

import lombok.Data;

@Data
public class LoginUserRequest {
    private String email;
    private String password;
}
