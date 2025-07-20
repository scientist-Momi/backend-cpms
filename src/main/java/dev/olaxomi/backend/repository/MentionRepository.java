package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.model.Mention;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface MentionRepository extends CrudRepository<Mention, Long> {
    List<Mention> findByMessage_MessageId(UUID messageId);
    List<Mention> findByAnnouncement_AnnouncementId(UUID announcementId);
}
