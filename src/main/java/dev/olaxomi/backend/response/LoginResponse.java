package dev.olaxomi.backend.response;

import dev.olaxomi.backend.enums.Permission;
import dev.olaxomi.backend.model.UserPermission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private String token;
    private Set<Permission> permission;
    private long expiresIn;
}
