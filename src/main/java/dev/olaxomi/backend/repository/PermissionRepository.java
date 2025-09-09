package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.model.UserPermission;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends CrudRepository<UserPermission, Long> {
    Optional<UserPermission> findByUserId(Long userId);
    Optional<UserPermission> findByUserEmail(String email);

}
