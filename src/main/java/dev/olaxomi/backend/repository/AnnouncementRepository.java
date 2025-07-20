package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.model.Announcement;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface AnnouncementRepository extends CrudRepository<Announcement, UUID> {
    List<Announcement> findByAuthorId(Integer authorId);
}
