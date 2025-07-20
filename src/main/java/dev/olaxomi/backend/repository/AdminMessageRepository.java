package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.model.AdminMessage;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface AdminMessageRepository extends CrudRepository<AdminMessage, UUID> {
    List<AdminMessage> findByRecipientId(Integer recipientId);
}
