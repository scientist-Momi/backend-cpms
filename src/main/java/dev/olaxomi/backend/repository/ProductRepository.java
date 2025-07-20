package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.dto.ProductSalesDto;
import dev.olaxomi.backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {
    Optional<Product> findByName(String productName);
    List<Product> findAllByOrderByCreatedAtDesc();
    boolean existsByName(String name);

//    @Query("SELECT new dev.olaxomi.backend.dto.ProductSalesDto(p.id, p.name, COUNT(t.id)) " +
//            "FROM Product p JOIN CustomerTransaction t ON t.product.id = p.id " +
//            "GROUP BY p.id, p.name")
//    Page<ProductSalesDto> findTopSellingProducts(Pageable pageable);

}
