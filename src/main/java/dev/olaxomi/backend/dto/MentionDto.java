package dev.olaxomi.backend.dto;

import dev.olaxomi.backend.enums.MentionType;
import lombok.Data;

import java.util.UUID;

@Data
public class MentionDto {
    private Long mentionId;
    private UUID entityId;
    private String displayName;
    private MentionType type;
}
