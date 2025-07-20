package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.model.WalletTransaction;
import org.springframework.data.repository.CrudRepository;

public interface WalletTransactionRepository extends CrudRepository<WalletTransaction, Long> {
}
