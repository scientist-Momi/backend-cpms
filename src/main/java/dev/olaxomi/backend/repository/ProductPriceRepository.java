package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.model.ProductPrice;
import org.springframework.data.repository.CrudRepository;

public interface ProductPriceRepository extends CrudRepository<ProductPrice, Long> {
}
