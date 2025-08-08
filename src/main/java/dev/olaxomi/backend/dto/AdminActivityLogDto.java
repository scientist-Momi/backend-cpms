package dev.olaxomi.backend.dto;

import dev.olaxomi.backend.enums.Permission;
import dev.olaxomi.backend.enums.TargetType;
import dev.olaxomi.backend.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminActivityLogDto {
    private User user;
    private String ipAddress;
    private Permission actionType;
    private TargetType targetType;
    private String targetId;
    private String details;
    private LocalDateTime createdAt;
}
