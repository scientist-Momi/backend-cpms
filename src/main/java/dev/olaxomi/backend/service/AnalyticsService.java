package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.ProductSalesDto;
import dev.olaxomi.backend.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CustomerTransactionRepository transactionRepository;
    private final CustomerTransactionDetailRepository transactionDetailRepository;
    private final CustomerRepository customerRepository;

    public AnalyticsService(UserRepository userRepository, ProductRepository productRepository, CustomerTransactionRepository transactionRepository, CustomerTransactionDetailRepository transactionDetailRepository, CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
        this.transactionDetailRepository = transactionDetailRepository;
        this.customerRepository = customerRepository;
    }

    public long countNewCustomers(LocalDate from, LocalDate to) {
        return customerRepository.countByCreatedAtBetween(from.atStartOfDay(), to.atTime(23,59,59));
    }

    public long countNewUsers(LocalDate from, LocalDate to) {
        return userRepository.countByCreatedAtBetween(from.atStartOfDay(), to.atTime(23,59,59));
    }

//    public long countActiveUsers(LocalDate from, LocalDate to) {
//        return userRepository.countActiveUsersBetween(from.atStartOfDay(), to.atTime(23,59,59)); // Define as needed
//    }

    public BigDecimal totalRevenue(LocalDate from, LocalDate to) {
        return transactionRepository.sumAmountBetween(from.atStartOfDay(), to.atTime(23,59,59));
    }

    public List<ProductSalesDto> mostPopularProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit); // 0 = first page, limit = page size
        return transactionDetailRepository.findTopSellingProducts(pageable).getContent();
    }


    public List<Map<String, Object>> transactionsPerDay() {
        List<Object[]> results = transactionRepository.countTransactionsPerDay();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", row[0]);
            map.put("count", row[1]);
            list.add(map);
        }
        return list;
    }

}
