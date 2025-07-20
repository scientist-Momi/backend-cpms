package dev.olaxomi.backend.service;

import dev.olaxomi.backend.enums.Permission;
import dev.olaxomi.backend.model.User;
import dev.olaxomi.backend.model.UserPermission;
import dev.olaxomi.backend.repository.PermissionRepository;
import dev.olaxomi.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public PermissionService(PermissionRepository permissionRepository, UserRepository userRepository) {
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    public UserPermission getPermissions(Long userId) {
        return permissionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User permissions not found"));
    }

    public UserPermission updatePermission(Long userId, Set<Permission> permissions){
        User userFound = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<UserPermission> existingPermissionOpt = permissionRepository.findByUserId(userFound.getId());
        UserPermission userPermission;
        if (existingPermissionOpt.isPresent()) {
            userPermission = existingPermissionOpt.get();
        } else {
            userPermission = new UserPermission();
            userPermission.setUser(userFound);
        }
        userPermission.setPermissions(permissions);
        return permissionRepository.save(userPermission);
    }
}
