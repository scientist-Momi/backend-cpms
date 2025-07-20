package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.AdminMessageDto;
import dev.olaxomi.backend.dto.MentionDto;
import dev.olaxomi.backend.model.AdminMessage;
import dev.olaxomi.backend.model.Mention;
import dev.olaxomi.backend.repository.AdminMessageRepository;
import dev.olaxomi.backend.repository.MentionRepository;
import dev.olaxomi.backend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdminMessageService {
    private final AdminMessageRepository messageRepo;
    private final UserRepository userRepo;
    private final MentionRepository mentionRepo;
    private final ModelMapper mapper;

    public AdminMessageService(AdminMessageRepository messageRepo, UserRepository userRepo, MentionRepository mentionRepo, ModelMapper mapper) {
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
        this.mentionRepo = mentionRepo;
        this.mapper = mapper;
    }

    public AdminMessageDto sendMessage(AdminMessageDto dto) {
        AdminMessage message = new AdminMessage();
        message.setMessageId(UUID.randomUUID());
        message.setSender(userRepo.findById(dto.getSenderId()).orElseThrow());
        message.setRecipient(userRepo.findById(dto.getRecipientId()).orElseThrow());
        message.setContent(dto.getContent());
        message.setStatus(dto.getStatus());
//        message.setCreatedAt(LocalDateTime.now());
//        message.setUpdatedAt(LocalDateTime.now());
        AdminMessage saved = messageRepo.save(message);

        // Save mentions
        if (dto.getMentions() != null) {
            for (MentionDto mDto : dto.getMentions()) {
                Mention mention = new Mention();
                mention.setEntityId(mDto.getEntityId());
                mention.setDisplayName(mDto.getDisplayName());
                mention.setType(mDto.getType());
                mention.setMessage(saved);
                mentionRepo.save(mention);
            }
        }
        return mapper.map(saved, AdminMessageDto.class);
    }

    public List<AdminMessageDto> getMessagesForRecipient(Integer recipientId) {
        return messageRepo.findByRecipientId(recipientId)
                .stream()
                .map(msg -> mapper.map(msg, AdminMessageDto.class))
                .toList();
    }
}
