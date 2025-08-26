package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.model.CustomerTransaction;
import dev.olaxomi.backend.model.ReturnTransaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReturnTransactionRepository extends CrudRepository<ReturnTransaction, Long> {
    List<ReturnTransaction> findAllByOrderByCreatedAtDesc();
    List<ReturnTransaction> findByCustomerCustomerId(UUID customerId);

    @Query("SELECT DISTINCT t FROM ReturnTransaction t JOIN t.returnDetails d WHERE d.product.id = :productId")
    List<ReturnTransaction> findByProductId(@Param("productId") Long productId);

    // In your ReturnTransactionRepository:
    List<ReturnTransaction> findByTransaction_TransactionId(Long transactionId);


}
