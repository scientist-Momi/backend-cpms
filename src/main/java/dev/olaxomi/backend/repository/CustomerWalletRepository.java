package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.model.CustomerWallet;
import org.springframework.data.repository.CrudRepository;

public interface CustomerWalletRepository extends CrudRepository<CustomerWallet, Long> {
}
