package dev.olaxomi.backend.controller;

import dev.olaxomi.backend.dto.ProductSalesDto;
import dev.olaxomi.backend.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/analytics")
public class AnalyticsController {
    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/new-users")
    public long getNewUsers(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return analyticsService.countNewUsers(from, to);
    }

    @GetMapping("/new-customers")
    public long getNewCustomers(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return analyticsService.countNewCustomers(from, to);
    }

//    @GetMapping("/active-users")
//    public long getActiveUsers(@RequestParam LocalDate from, @RequestParam LocalDate to) {
//        return analyticsService.countActiveUsers(from, to);
//    }

    @GetMapping("/revenue")
    public BigDecimal getRevenue(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return analyticsService.totalRevenue(from, to);
    }

    @GetMapping("/transactions-per-day")
    public List<Map<String, Object>> getTransactionsPerDay() {
        return analyticsService.transactionsPerDay();
    }

    @GetMapping("/most-popular-products")
    public List<ProductSalesDto> getMostPopularProducts(@RequestParam(defaultValue = "5") int limit) {
        return analyticsService.mostPopularProducts(limit);
    }
}
