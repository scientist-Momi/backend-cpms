package dev.olaxomi.backend.dto;

import dev.olaxomi.backend.enums.Role;
import dev.olaxomi.backend.model.UserPermission;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private boolean enabled;
//    private UserPermission userPermission;
    private LocalDateTime createdAt;
}
