package dev.olaxomi.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class AnnouncementDto {
    private UUID announcementId;
    private String title;
    private String content;
    private Long authorId;
    private String authorDisplayName;         // Optional, for UI display
    private List<MentionDto> mentions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
