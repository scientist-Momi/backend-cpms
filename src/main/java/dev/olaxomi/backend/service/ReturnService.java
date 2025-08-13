package dev.olaxomi.backend.service;

import dev.olaxomi.backend.mapper.CustomerTransactionMapper;
import dev.olaxomi.backend.repository.*;
import org.springframework.stereotype.Service;

@Service
public class ReturnService {
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CustomerTransactionRepository customerTransactionRepository;
    private final CustomerWalletRepository walletRepository;
    private final CustomerTransactionMapper customerTransactionMapper;
    private final WalletTransactionRepository walletTransactionRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AdminActivityService activityService;

    public ReturnService(CustomerRepository customerRepository, ProductRepository productRepository, CustomerTransactionRepository customerTransactionRepository, CustomerWalletRepository walletRepository, CustomerTransactionMapper customerTransactionMapper, WalletTransactionRepository walletTransactionRepository, ProductVariantRepository productVariantRepository, AdminActivityService activityService) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.customerTransactionRepository = customerTransactionRepository;
        this.walletRepository = walletRepository;
        this.customerTransactionMapper = customerTransactionMapper;
        this.walletTransactionRepository = walletTransactionRepository;
        this.productVariantRepository = productVariantRepository;
        this.activityService = activityService;
    }
}
