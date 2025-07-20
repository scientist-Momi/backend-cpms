package dev.olaxomi.backend.repository;

import dev.olaxomi.backend.dto.ProductSalesDto;
import dev.olaxomi.backend.model.CustomerTransactionDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerTransactionDetailRepository extends CrudRepository<CustomerTransactionDetail, Long> {
    @Query("SELECT new dev.olaxomi.backend.dto.ProductSalesDto(d.product.id, d.product.name, COUNT(d.id)) " +
            "FROM CustomerTransactionDetail d " +
            "GROUP BY d.product.id, d.product.name " +
            "ORDER BY COUNT(d.id) DESC")
    Page<ProductSalesDto> findTopSellingProducts(Pageable pageable);

}
