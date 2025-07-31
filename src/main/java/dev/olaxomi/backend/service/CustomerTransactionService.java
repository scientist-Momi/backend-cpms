package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.CustomerDto;
import dev.olaxomi.backend.dto.CustomerTransactionDetailDto;
import dev.olaxomi.backend.dto.CustomerTransactionDto;
import dev.olaxomi.backend.enums.TransactionType;
import dev.olaxomi.backend.mapper.CustomerMapper;
import dev.olaxomi.backend.mapper.CustomerTransactionMapper;
import dev.olaxomi.backend.model.*;
import dev.olaxomi.backend.repository.*;
import dev.olaxomi.backend.request.CustomerDepositRequest;
import dev.olaxomi.backend.request.NewCustomerTransactionDetailRequest;
import dev.olaxomi.backend.request.NewCustomerTransactionRequest;
import dev.olaxomi.backend.request.UpdateCustomerTransactionRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerTransactionService {
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CustomerTransactionDetailRepository transactionDetailRepository;
    private final CustomerTransactionRepository customerTransactionRepository;
    private final CustomerWalletRepository walletRepository;
    private final CustomerTransactionMapper customerTransactionMapper;
    private final WalletTransactionRepository walletTransactionRepository;
    private final CustomerMapper customerMapper;

    public CustomerTransactionService(CustomerRepository customerRepository, ProductRepository productRepository, CustomerTransactionDetailRepository transactionDetailRepository, CustomerTransactionRepository customerTransactionRepository, CustomerWalletRepository walletRepository, CustomerTransactionMapper customerTransactionMapper, WalletTransactionRepository walletTransactionRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.transactionDetailRepository = transactionDetailRepository;
        this.customerTransactionRepository = customerTransactionRepository;
        this.walletRepository = walletRepository;
        this.customerTransactionMapper = customerTransactionMapper;
        this.walletTransactionRepository = walletTransactionRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional
    public List<CustomerTransactionDto> allTransactions() {
        List<CustomerTransaction> transactions = customerTransactionRepository.findAllByOrderByCreatedAtDesc();
        return customerTransactionMapper.toDtoList(transactions);
    }

    public List<CustomerTransactionDto> getTransactionsForCustomer(UUID customerId){
        List<CustomerTransaction> transactions = customerTransactionRepository.findByCustomerCustomerId(customerId);
        return customerTransactionMapper.toDtoList(transactions);
    }

    public CustomerTransactionDto getTransaction(Long transactionId){
        CustomerTransaction transaction = customerTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Customer Transaction not found!"));
        return customerTransactionMapper.toDto(transaction);
    }

    public List<CustomerTransactionDto> getTransactionsByProduct(Long productId) {
        List<CustomerTransaction> transactions = customerTransactionRepository.findByProductId(productId);
        return customerTransactionMapper.toDtoList(transactions);
    }

    public List<CustomerTransactionDto> getTransactionsByCustomerAndProduct(UUID customerId, Long productId) {
        List<CustomerTransaction> transactions = customerTransactionRepository.findByCustomerIdAndProductId(customerId, productId);
        return customerTransactionMapper.toDtoList(transactions);
    }

    @Transactional
    public CustomerTransactionDto addCustomerTransaction(NewCustomerTransactionRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        CustomerWallet wallet = customer.getCustomerWallet();
        if (wallet == null) throw new IllegalStateException("Customer does not have a wallet.");

        int totalQuantity = 0;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (NewCustomerTransactionDetailRequest detail : request.getTransactionDetails()) {
            totalQuantity += detail.getQuantity();
            BigDecimal lineDiscount = detail.getLineDiscount() != null ? detail.getLineDiscount() : BigDecimal.ZERO;
            totalDiscount = totalDiscount.add(lineDiscount);

            BigDecimal lineTotal = detail.getUnitPrice()
                    .multiply(BigDecimal.valueOf(detail.getQuantity()))
                    .subtract(lineDiscount);
            totalAmount = totalAmount.add(lineTotal);
        }

        wallet.setBalance(wallet.getBalance().subtract(totalAmount));
        walletRepository.save(wallet);

        WalletTransaction walletTx = new WalletTransaction();
        walletTx.setWallet(wallet);
        walletTx.setAmount(totalAmount.negate());
        walletTx.setBalanceAfterTransaction(wallet.getBalance());
        walletTx.setTransactionType(TransactionType.PURCHASE);
        walletTx.setReference("Purchase on " + LocalDateTime.now());
        walletTx.setDescription("Purchase of products");
        walletTransactionRepository.save(walletTx);

        CustomerTransaction transaction = new CustomerTransaction();
        transaction.setCustomer(customer);
        transaction.setTotalAmount(totalAmount);
        transaction.setTotalQuantity(totalQuantity);
        transaction.setTotalDiscount(totalDiscount);

        List<CustomerTransactionDetail> details = new ArrayList<>();
        for (NewCustomerTransactionDetailRequest detailReq : request.getTransactionDetails()) {
            Product product = productRepository.findById(detailReq.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));

            CustomerTransactionDetail detail = new CustomerTransactionDetail();
            detail.setTransaction(transaction);
            detail.setProduct(product);
            detail.setQuantity(detailReq.getQuantity());
            detail.setUnitPrice(detailReq.getUnitPrice());
            detail.setLineDiscount(detailReq.getLineDiscount());
            details.add(detail);
        }
        transaction.setTransactionDetails(details);

        CustomerTransaction savedTransaction = customerTransactionRepository.save(transaction);

        return customerTransactionMapper.toDto(savedTransaction);
    }

    @Transactional
    public CustomerTransactionDto updateTransaction(Long transactionId, UpdateCustomerTransactionRequest request
    ) {
        CustomerTransaction transaction = customerTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        BigDecimal oldTotal = transaction.getTotalAmount();

        List<CustomerTransactionDetail> newDetails = new ArrayList<>();
        for (NewCustomerTransactionDetailRequest detailDto : request.getTransactionDetails()) {
            Product product = productRepository.findById(detailDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));

            CustomerTransactionDetail detail = new CustomerTransactionDetail();
            detail.setTransaction(transaction);
            detail.setProduct(product);
            detail.setQuantity(detailDto.getQuantity());
            detail.setUnitPrice(detailDto.getUnitPrice());
            detail.setLineDiscount(detailDto.getLineDiscount());
            newDetails.add(detail);
        }
        transaction.getTransactionDetails().addAll(newDetails);

        BigDecimal newTotal = calculateTotalAmount(newDetails);
        int newQuantity = calculateTotalQuantity(newDetails);
        BigDecimal newDiscount = calculateTotalDiscount(newDetails);

        transaction.setTotalAmount(newTotal);
        transaction.setTotalQuantity(newQuantity);
        transaction.setTotalDiscount(newDiscount);

        CustomerWallet wallet = transaction.getCustomer().getCustomerWallet();
        BigDecimal balanceChange = newTotal.subtract(oldTotal);
        wallet.setBalance(wallet.getBalance().subtract(balanceChange));
        walletRepository.save(wallet);

        CustomerTransaction updatedTransaction = customerTransactionRepository.save(transaction);
        return customerTransactionMapper.toDto(updatedTransaction);
    }

    private BigDecimal calculateTotalAmount(List<CustomerTransactionDetail> details) {
        return details.stream()
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity()))
                        .subtract(d.getLineDiscount() != null ? d.getLineDiscount() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int calculateTotalQuantity(List<CustomerTransactionDetail> details) {
        return details.stream().mapToInt(CustomerTransactionDetail::getQuantity).sum();
    }

    private BigDecimal calculateTotalDiscount(List<CustomerTransactionDetail> details) {
        return details.stream()
                .map(d -> d.getLineDiscount() != null ? d.getLineDiscount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public CustomerDto processDeposit(CustomerDepositRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        CustomerWallet wallet = customer.getCustomerWallet();
        if (wallet == null) throw new IllegalStateException("Customer does not have a wallet.");

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        WalletTransaction walletTx = new WalletTransaction();
        walletTx.setWallet(wallet);
        walletTx.setAmount(request.getAmount());
        walletTx.setBalanceAfterTransaction(wallet.getBalance());
        walletTx.setTransactionType(TransactionType.DEPOSIT);
        walletTx.setReference(request.getNote() != null ? request.getNote() : "Deposit on " + LocalDateTime.now());
        walletTransactionRepository.save(walletTx);

        return customerMapper.toDto(customer);
    }

}
