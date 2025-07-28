package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.model.ProductVariant;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductVariantRepository extends CrudRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);
    boolean existsByProductIdAndWeight(Long productId, double weight);
}
