package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.AnnouncementDto;
import dev.olaxomi.backend.dto.MentionDto;
import dev.olaxomi.backend.model.Announcement;
import dev.olaxomi.backend.model.Mention;
import dev.olaxomi.backend.repository.AnnouncementRepository;
import dev.olaxomi.backend.repository.MentionRepository;
import dev.olaxomi.backend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AnnouncementService {
    private final AnnouncementRepository announcementRepo;
    private final UserRepository userRepo;
    private final MentionRepository mentionRepo;
    private final ModelMapper mapper;

    public AnnouncementService(AnnouncementRepository announcementRepo, UserRepository userRepo, MentionRepository mentionRepo, ModelMapper mapper) {
        this.announcementRepo = announcementRepo;
        this.userRepo = userRepo;
        this.mentionRepo = mentionRepo;
        this.mapper = mapper;
    }

    public AnnouncementDto createAnnouncement(AnnouncementDto dto) {
        Announcement announcement = new Announcement();
        announcement.setAnnouncementId(UUID.randomUUID());
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setAuthor(userRepo.findById(dto.getAuthorId()).orElseThrow());
//        announcement.setCreatedAt(LocalDateTime.now());
//        announcement.setUpdatedAt(LocalDateTime.now());
        Announcement saved = announcementRepo.save(announcement);

        // Save mentions
        if (dto.getMentions() != null) {
            for (MentionDto mDto : dto.getMentions()) {
                Mention mention = new Mention();
                mention.setEntityId(mDto.getEntityId());
                mention.setDisplayName(mDto.getDisplayName());
                mention.setType(mDto.getType());
                mention.setAnnouncement(saved);
                mentionRepo.save(mention);
            }
        }
        return mapper.map(saved, AnnouncementDto.class);
    }

    public List<AnnouncementDto> getAnnouncementsByAuthor(Integer authorId) {
        return announcementRepo.findByAuthorId(authorId)
                .stream()
                .map(a -> mapper.map(a, AnnouncementDto.class))
                .toList();
    }
}
