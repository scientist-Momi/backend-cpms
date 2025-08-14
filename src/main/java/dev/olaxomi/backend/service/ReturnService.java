package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.ReturnTransactionDetailDto;
import dev.olaxomi.backend.dto.ReturnTransactionDto;
import dev.olaxomi.backend.mapper.CustomerTransactionMapper;
import dev.olaxomi.backend.mapper.ReturnTransactionMapper;
import dev.olaxomi.backend.model.Customer;
import dev.olaxomi.backend.model.CustomerTransaction;
import dev.olaxomi.backend.model.CustomerWallet;
import dev.olaxomi.backend.model.ReturnTransaction;
import dev.olaxomi.backend.repository.*;
import dev.olaxomi.backend.request.ReturnRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
    private final ReturnTransactionMapper returnTransactionMapper;
    private final ReturnTransactionRepository returnRepository;

    public ReturnService(CustomerRepository customerRepository, ProductRepository productRepository, CustomerTransactionRepository customerTransactionRepository, CustomerWalletRepository walletRepository, CustomerTransactionMapper customerTransactionMapper, WalletTransactionRepository walletTransactionRepository, ProductVariantRepository productVariantRepository, AdminActivityService activityService, ReturnTransactionMapper returnTransactionMapper, ReturnTransactionRepository returnRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.customerTransactionRepository = customerTransactionRepository;
        this.walletRepository = walletRepository;
        this.customerTransactionMapper = customerTransactionMapper;
        this.walletTransactionRepository = walletTransactionRepository;
        this.productVariantRepository = productVariantRepository;
        this.activityService = activityService;
        this.returnTransactionMapper = returnTransactionMapper;
        this.returnRepository = returnRepository;
    }

    public List<ReturnTransactionDto> allReturns(){
        List<ReturnTransaction> transactions = returnRepository.findAllByOrderByCreatedAtDesc();
        return returnTransactionMapper.toDtoList(transactions);
    }

    public List<ReturnTransactionDto> getReturnsForCustomer(UUID customerId){
        List<ReturnTransaction> transactions = returnRepository.findByCustomerCustomerId(customerId);
        return returnTransactionMapper.toDtoList(transactions);
    }

    public ReturnTransactionDto getReturn(Long returnId){
        ReturnTransaction transaction = returnRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return Transaction not found!"));
        return returnTransactionMapper.toDto(transaction);
    }

    public List<ReturnTransactionDto> getReturnsByProduct(Long productId){
        List<ReturnTransaction> transactions = returnRepository.findByProductId(productId);
        return returnTransactionMapper.toDtoList(transactions);
    }

    public ReturnTransactionDto processReturn(ReturnRequest request){
        CustomerTransaction purchase = customerTransactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found"));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        CustomerWallet wallet = customer.getCustomerWallet();
        if (wallet == null) throw new IllegalStateException("Customer does not have a wallet.");
    }
}
