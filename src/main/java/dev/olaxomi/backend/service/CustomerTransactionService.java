package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.CustomerDto;
import dev.olaxomi.backend.dto.CustomerTransactionDetailDto;
import dev.olaxomi.backend.dto.CustomerTransactionDto;
import dev.olaxomi.backend.enums.ActionType;
import dev.olaxomi.backend.enums.TargetType;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomerTransactionService {
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CustomerTransactionDetailRepository transactionDetailRepository;
    private final CustomerTransactionRepository customerTransactionRepository;
    private final CustomerWalletRepository walletRepository;
    private final CustomerTransactionMapper customerTransactionMapper;
    private final WalletTransactionRepository walletTransactionRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CustomerMapper customerMapper;
    private final AdminActivityService activityService;

    public CustomerTransactionService(CustomerRepository customerRepository, ProductRepository productRepository, CustomerTransactionDetailRepository transactionDetailRepository, CustomerTransactionRepository customerTransactionRepository, CustomerWalletRepository walletRepository, CustomerTransactionMapper customerTransactionMapper, WalletTransactionRepository walletTransactionRepository, ProductVariantRepository productVariantRepository, CustomerMapper customerMapper, AdminActivityService activityService) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.transactionDetailRepository = transactionDetailRepository;
        this.customerTransactionRepository = customerTransactionRepository;
        this.walletRepository = walletRepository;
        this.customerTransactionMapper = customerTransactionMapper;
        this.walletTransactionRepository = walletTransactionRepository;
        this.productVariantRepository = productVariantRepository;
        this.customerMapper = customerMapper;
        this.activityService = activityService;
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

        Map<Long, ProductVariant> variantCache = new HashMap<>();
        Map<Long, Product> productCache = new HashMap<>();

        for (NewCustomerTransactionDetailRequest detail : request.getTransactionDetails()) {
            Long variantId = detail.getVariantId();
            Long productId = detail.getProductId();

            ProductVariant variant = variantCache.computeIfAbsent(variantId, id ->
                    productVariantRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Product variant not found for ID: " + id))
            );

            Product product = variant.getProduct();
            if (product == null) {
                throw new EntityNotFoundException("Product not found for variant ID: " + variantId);
            }

            productCache.putIfAbsent(product.getId(), product);

            if (productId != null && !product.getId().equals(productId)) {
                throw new IllegalArgumentException(
                        "Variant ID " + variantId + " does not belong to product ID " + productId);
            }

            BigDecimal unitPrice = product.getLatestPrice();
            BigDecimal weight = BigDecimal.valueOf(variant.getWeight());
            int quantity = detail.getQuantity();

            int previousSold = product.getQuantitySold();
            product.setQuantitySold(previousSold + quantity);

            BigDecimal lineDiscount = detail.getLineDiscount() != null ? detail.getLineDiscount() : BigDecimal.ZERO;

            BigDecimal lineTotal = weight.multiply(unitPrice)
                    .multiply(BigDecimal.valueOf(quantity))
                    .subtract(lineDiscount);

            totalQuantity += quantity;
            totalDiscount = totalDiscount.add(lineDiscount);
            totalAmount = totalAmount.add(lineTotal);
        }

        for (Product product : productCache.values()) {
            productRepository.save(product);
        }

        BigDecimal newBalance = getNewBalance(customer, wallet, totalAmount);

        wallet.setBalance(newBalance);
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
            Product product = productCache.get(detailReq.getProductId());
            ProductVariant variant = variantCache.get(detailReq.getVariantId());

            CustomerTransactionDetail detail = new CustomerTransactionDetail();
            detail.setTransaction(transaction);
            detail.setProduct(product);
            detail.setVariant(variant);
            detail.setQuantity(detailReq.getQuantity());
            detail.setUnitPrice(product.getLatestPrice());
            detail.setLineDiscount(detailReq.getLineDiscount());
            details.add(detail);
        }
        transaction.setTransactionDetails(details);

        CustomerTransaction savedTransaction = customerTransactionRepository.save(transaction);

        String logDetails = String.format(
                "Created transaction ID %d for customer ID %s with totalAmount %s and totalQuantity %d",
                savedTransaction.getTransactionId(),
                customer.getCustomerId(),
                totalAmount.toPlainString(),
                totalQuantity
        );

        activityService.logActivity(
                ActionType.CREATE_TRANSACTION,
                TargetType.TRANSACTION,
                String.valueOf(savedTransaction.getTransactionId()),
                logDetails
        );
        return customerTransactionMapper.toDto(savedTransaction);
    }

    @Transactional
    public CustomerTransactionDto addCustomerTransaction2(NewCustomerTransactionRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        CustomerWallet wallet = customer.getCustomerWallet();
        if (wallet == null) throw new IllegalStateException("Customer does not have a wallet.");

        int totalQuantity = 0;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        Map<Long, ProductVariant> variantCache = new HashMap<>();
        Map<Long, Product> productCache = new HashMap<>();

        for (NewCustomerTransactionDetailRequest detail : request.getTransactionDetails()) {
            Long variantId = detail.getVariantId();
            Long productId = detail.getProductId();

            ProductVariant variant = variantCache.computeIfAbsent(variantId, id ->
                    productVariantRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Product variant not found for ID: " + id))
            );

            Product product = variant.getProduct();
            if (product == null) {
                throw new EntityNotFoundException("Product not found for variant ID: " + variantId);
            }

            productCache.putIfAbsent(product.getId(), product);

            if (productId != null && !product.getId().equals(productId)) {
                throw new IllegalArgumentException(
                        "Variant ID " + variantId + " does not belong to product ID " + productId);
            }

            BigDecimal unitPrice = product.getLatestPrice();
            BigDecimal weight = BigDecimal.valueOf(variant.getWeight());
            int quantity = detail.getQuantity();

            int previousSold = product.getQuantitySold();
            product.setQuantitySold(previousSold + quantity);

            BigDecimal lineDiscount = detail.getLineDiscount() != null ? detail.getLineDiscount() : BigDecimal.ZERO;

            BigDecimal lineTotal = weight.multiply(unitPrice)
                    .multiply(BigDecimal.valueOf(quantity))
                    .subtract(lineDiscount);

            totalQuantity += quantity;
            totalDiscount = totalDiscount.add(lineDiscount);
            totalAmount = totalAmount.add(lineTotal);
        }

        for (Product product : productCache.values()) {
            productRepository.save(product);
        }

        BigDecimal newBalance = getNewBalance(customer, wallet, totalAmount);

        wallet.setBalance(newBalance);
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

//        List<CustomerTransactionDetail> details = new ArrayList<>();

        Map<String, AggregatedDetail> aggregatedDetails = new HashMap<>();
        for (NewCustomerTransactionDetailRequest detail : request.getTransactionDetails()) {
            String key = detail.getProductId() + "-" + detail.getVariantId();
            AggregatedDetail aggDetail = aggregatedDetails.get(key);
            if (aggDetail == null) {
                aggDetail = new AggregatedDetail(detail.getProductId(), detail.getVariantId(),
                        detail.getQuantity(), detail.getLineDiscount() != null ? detail.getLineDiscount() : BigDecimal.ZERO);
                aggregatedDetails.put(key, aggDetail);
            } else {
                aggDetail.quantity += detail.getQuantity();
                aggDetail.lineDiscount = aggDetail.lineDiscount.add(detail.getLineDiscount() != null ? detail.getLineDiscount() : BigDecimal.ZERO);
            }
        }

        List<CustomerTransactionDetail> details = new ArrayList<>();
        for (AggregatedDetail agg : aggregatedDetails.values()) {
            Product product = productCache.get(agg.productId);
            ProductVariant variant = variantCache.get(agg.variantId);

            CustomerTransactionDetail detail = new CustomerTransactionDetail();
            detail.setTransaction(transaction);
            detail.setProduct(product);
            detail.setVariant(variant);
            detail.setQuantity(agg.quantity);
            detail.setUnitPrice(product.getLatestPrice());
            detail.setLineDiscount(agg.lineDiscount);
            details.add(detail);
        }
        transaction.setTransactionDetails(details);


        CustomerTransaction savedTransaction = customerTransactionRepository.save(transaction);

        String logDetails = String.format(
                "Created transaction ID %d for customer ID %s with totalAmount %s and totalQuantity %d",
                savedTransaction.getTransactionId(),
                customer.getCustomerId(),
                totalAmount.toPlainString(),
                totalQuantity
        );

        activityService.logActivity(
                ActionType.CREATE_TRANSACTION,
                TargetType.TRANSACTION,
                String.valueOf(savedTransaction.getTransactionId()),
                logDetails
        );
        return customerTransactionMapper.toDto(savedTransaction);
    }

    private static BigDecimal getNewBalance(Customer customer, CustomerWallet wallet, BigDecimal totalAmount) {
        BigDecimal creditLimit = customer.getCreditLimit() != null ? customer.getCreditLimit() : BigDecimal.ZERO;
        BigDecimal newBalance = wallet.getBalance().subtract(totalAmount);
        BigDecimal negativeCreditLimit = creditLimit.negate();

        if (newBalance.compareTo(negativeCreditLimit) < 0) {
            throw new IllegalStateException(
                    "Transaction declined: wallet balance cannot go below -" + creditLimit.toPlainString() + " due to credit limit."
            );
        }
        return newBalance;
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

        String logDetails = String.format(
                "Processed deposit transaction ID %d for customer ID %s with amount %s. New balance: %s",
                walletTx.getTransactionId(),
                customer.getCustomerId(),
                request.getAmount().toPlainString(),
                wallet.getBalance().toPlainString()
        );

        activityService.logActivity(
                ActionType.CUSTOMER_DEPOSIT, // or ActionType.DEPOSIT if you have it
                TargetType.CUSTOMER,
                String.valueOf(walletTx.getTransactionId()),
                logDetails
        );

        return customerMapper.toDto(customer);
    }

//    public CustomerDto processReturn(CustomerReturnRequest request) {
//        CustomerTransaction purchase = customerTransactionRepository.findById(request.getPurchaseId())
//                .orElseThrow(() -> new EntityNotFoundException("Purchase not found"));
//
//        Product product = productRepository.findById(request.getProductId())
//                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
//
//        if (request.getReturnQuantity() > purchase.getQuantityPurchased()) {
//            throw new IllegalArgumentException("Return quantity exceeds purchased quantity");
//        }
//
//        // Adjust inventory
//        product.setInventory(product.getInventory() + request.getReturnQuantity());
//        productRepository.save(product);
//
//        // Create return transaction
//        ReturnTransaction returnTx = new ReturnTransaction();
//        returnTx.setPurchase(purchase);
//        returnTx.setProduct(product);
//        returnTx.setQuantity(request.getReturnQuantity());
//        returnTx.setReturnDate(LocalDateTime.now());
//        returnTransactionRepository.save(returnTx);
//
//        // Adjust customer wallet or refund accordingly (if applicable)
//
//        // Log activity
//        String logDetails = String.format(
//                "Processed return of %d units of product ID %d for customer ID %s",
//                request.getReturnQuantity(),
//                product.getId(),
//                purchase.getCustomer().getCustomerId()
//        );
//
//        activityService.logActivity(
//                ActionType.PROCESS_RETURN,
//                TargetType.RETURN,
//                String.valueOf(returnTx.getId()),
//                logDetails
//        );
//
//        return customerMapper.toDto(purchase.getCustomer());
//    }

    private static class AggregatedDetail {
        Long productId;
        Long variantId;
        int quantity;
        BigDecimal lineDiscount;

        public AggregatedDetail(Long productId, Long variantId, int quantity, BigDecimal lineDiscount) {
            this.productId = productId;
            this.variantId = variantId;
            this.quantity = quantity;
            this.lineDiscount = lineDiscount;
        }
    }

}
