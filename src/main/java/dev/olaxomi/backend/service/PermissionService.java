package dev.olaxomi.backend.service;

import dev.olaxomi.backend.enums.ActionType;
import dev.olaxomi.backend.enums.Permission;
import dev.olaxomi.backend.enums.TargetType;
import dev.olaxomi.backend.model.User;
import dev.olaxomi.backend.model.UserPermission;
import dev.olaxomi.backend.repository.PermissionRepository;
import dev.olaxomi.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final AdminActivityService activityService;

    public PermissionService(PermissionRepository permissionRepository, UserRepository userRepository, AdminActivityService activityService) {
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
    }

    public UserPermission getPermissions(Long userId) {
        return permissionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User permissions not found"));
    }

    public UserPermission getPermissionsByEmail(String email) {
        return permissionRepository.findByUserEmail(email)
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

        String logDetails = String.format(
                "Updated permissions for user ID %d",
                userFound.getId()
        );

        activityService.logActivity(
                ActionType.UPDATE_PERMISSION,
                TargetType.USER,
                String.valueOf(userFound.getId()),
                logDetails
        );
        return permissionRepository.save(userPermission);
    }
}
