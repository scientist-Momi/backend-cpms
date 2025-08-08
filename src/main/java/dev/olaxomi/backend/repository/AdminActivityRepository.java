package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.model.AdminActivityLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminActivityRepository extends CrudRepository<AdminActivityLog, Long> {
    @Query("SELECT l FROM admin_activity_log l ORDER BY l.createdAt DESC")
    List<AdminActivityLog> findAllOrderByCreatedAtDesc();

    List<AdminActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
