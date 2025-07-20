package dev.olaxomi.backend.request;

import dev.olaxomi.backend.enums.Permission;
import lombok.Data;

import java.util.Set;

@Data
public class UpdatePermissionRequest {
    private Set<Permission> permissions;
}
