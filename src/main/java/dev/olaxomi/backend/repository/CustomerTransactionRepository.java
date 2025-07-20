package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.model.CustomerTransaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerTransactionRepository extends CrudRepository<CustomerTransaction, Long> {
    List<CustomerTransaction> findByCustomerCustomerId(UUID customerId);
    List<CustomerTransaction> findAllByOrderByCreatedAtDesc();
    @Query("SELECT DISTINCT t FROM CustomerTransaction t JOIN t.transactionDetails d WHERE d.product.id = :productId")
    List<CustomerTransaction> findByProductId(@Param("productId") Long productId);
    @Query("SELECT DISTINCT t FROM CustomerTransaction t JOIN t.transactionDetails d WHERE t.customer.customerId = :customerId AND d.product.id = :productId")
    List<CustomerTransaction> findByCustomerIdAndProductId(@Param("customerId") UUID customerId, @Param("productId") Long productId);

    @Query("SELECT SUM(t.totalAmount) FROM CustomerTransaction t WHERE t.createdAt BETWEEN :from AND :to")
    BigDecimal sumAmountBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT DATE(t.createdAt) as day, COUNT(t.id) as count FROM CustomerTransaction t GROUP BY day ORDER BY day")
    List<Object[]> countTransactionsPerDay();
}
