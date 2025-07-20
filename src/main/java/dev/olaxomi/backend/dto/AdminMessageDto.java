package dev.olaxomi.backend.dto;

import dev.olaxomi.backend.enums.MessageStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class AdminMessageDto {
    private UUID messageId;
    private Long senderId;
    private String senderDisplayName;
    private Long recipientId;
    private String recipientDisplayName;
    private String content;
    private MessageStatus status;
    private List<MentionDto> mentions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
