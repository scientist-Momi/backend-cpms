package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, UUID> {
//    Optional<Customer> findById(UUID customerId);
    Optional<Customer> findByName(String customerName);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<Customer> findByNameStartingWithIgnoreCase(String query);
    Page<Customer> findByNameStartingWithIgnoreCase(String query, Pageable pageable);
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT c FROM Customer c ORDER BY c.createdAt DESC")
    List<Customer> findAllOrderByCreatedAtDesc();
}
