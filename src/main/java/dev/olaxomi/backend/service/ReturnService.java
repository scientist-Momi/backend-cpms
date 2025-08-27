package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.ReturnTransactionDetailDto;
import dev.olaxomi.backend.dto.ReturnTransactionDto;
import dev.olaxomi.backend.enums.ActionType;
import dev.olaxomi.backend.enums.TargetType;
import dev.olaxomi.backend.enums.TransactionType;
import dev.olaxomi.backend.mapper.CustomerTransactionMapper;
import dev.olaxomi.backend.mapper.ReturnTransactionMapper;
import dev.olaxomi.backend.model.*;
import dev.olaxomi.backend.repository.*;
import dev.olaxomi.backend.request.ReturnDetailRequest;
import dev.olaxomi.backend.request.ReturnRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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

    public List<ReturnTransactionDto> getReturnsByTransaction(Long transactionId) {
        List<ReturnTransaction> transactions = returnRepository.findByTransaction_TransactionIdOrderByCreatedAtDesc(transactionId);
        return returnTransactionMapper.toDtoList(transactions);
    }


    public ReturnTransactionDto processReturn(ReturnRequest request){
        CustomerTransaction transaction = customerTransactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found"));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        CustomerWallet wallet = customer.getCustomerWallet();
        if (wallet == null) throw new IllegalStateException("Customer does not have a wallet.");

        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        Map<Long, ProductVariant> variantCache = new HashMap<>();
        Map<Long, Product> productCache = new HashMap<>();

        List<ReturnTransactionDetail> returnDetails = new ArrayList<>();

        for (ReturnDetailRequest detailReq : request.getReturnDetails()) {
            Long variantId = detailReq.getVariantId();
            Long productId = detailReq.getProductId();

            ProductVariant variant = variantCache.computeIfAbsent(variantId, id ->
                    productVariantRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Product variant not found for ID: " + id))
            );

            Product product = variant.getProduct();

            if (product == null) throw new EntityNotFoundException("Product not found for variant ID: " + variantId);
            productCache.putIfAbsent(product.getId(), product);

            if (productId != null && !product.getId().equals(productId)) {
                throw new IllegalArgumentException(
                        "Variant ID " + variantId + " does not belong to product ID " + productId);
            }

            CustomerTransactionDetail originalDetail = transaction.getTransactionDetails().stream()
                    .filter(td -> td.getProduct().getId().equals(product.getId())
                            && td.getVariant().getId().equals(variant.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Original transaction detail not found for product/variant"));

            if (detailReq.getQuantity() > originalDetail.getQuantity()) {
                throw new IllegalArgumentException("Return quantity exceeds purchased quantity for product/variant");
            }

            variant.setInventory(variant.getInventory() + detailReq.getQuantity());

            BigDecimal weight = BigDecimal.valueOf(variant.getWeight());
            BigDecimal unitPrice = originalDetail.getUnitPrice();
            int quantity = detailReq.getQuantity();
//            BigDecimal lineDiscount = detailReq.getLineDiscount() != null ? detailReq.getLineDiscount() : BigDecimal.ZERO;

            BigDecimal lineTotal = weight.multiply(unitPrice)
                    .multiply(BigDecimal.valueOf(quantity));
//                    .subtract(lineDiscount);

            totalQuantity += quantity;
//            totalDiscount = totalDiscount.add(lineDiscount);
            totalAmount = totalAmount.add(lineTotal);

            ReturnTransactionDetail returnDetail = new ReturnTransactionDetail();
            returnDetail.setProduct(product);
            returnDetail.setVariant(variant);
            returnDetail.setQuantity(quantity);
            returnDetail.setUnitPrice(unitPrice);
//            returnDetail.setLineDiscount(lineDiscount);
            returnDetails.add(returnDetail);
        }

        for (ProductVariant variant : variantCache.values()) {
            productVariantRepository.save(variant);
        }

        for (Product product : productCache.values()) {
            productRepository.save(product); // If you want to adjust quantitySold, do so before saving
        }

        BigDecimal newWalletBalance = wallet.getBalance().add(totalAmount);
        wallet.setBalance(newWalletBalance);
        walletRepository.save(wallet);

        WalletTransaction walletTx = new WalletTransaction();
        walletTx.setWallet(wallet);
        walletTx.setAmount(totalAmount); // positive because it's a refund
        walletTx.setBalanceAfterTransaction(wallet.getBalance());
        walletTx.setTransactionType(TransactionType.REFUND);
        walletTx.setReference("Refund/Return on " + LocalDateTime.now());
        walletTx.setDescription("Product return refund");
        walletTransactionRepository.save(walletTx);

        ReturnTransaction returnTx = new ReturnTransaction();
        returnTx.setCustomer(customer);
        returnTx.setTransaction(transaction);
        returnTx.setTotalAmount(totalAmount);
        returnTx.setTotalQuantity(totalQuantity);
//        returnTx.setTotalDiscount(totalDiscount);
        returnTx.setReason(request.getReason());

        for (ReturnTransactionDetail detail : returnDetails) {
            detail.setReturnTransaction(returnTx);
        }
        returnTx.setReturnDetails(returnDetails);

        ReturnTransaction savedReturnTx = returnRepository.save(returnTx);
        updateCustomerTransaction(transaction, request.getReturnDetails());


        String logDetails = String.format(
                "Processed return transaction ID %d for customer ID %s, refund amount %s, total quantity %d",
                savedReturnTx.getReturnId(),
                customer.getCustomerId(),
                totalAmount.toPlainString(),
                totalQuantity
        );
        activityService.logActivity(
                ActionType.PROCESS_RETURN,
                TargetType.PRODUCT,
                String.valueOf(savedReturnTx.getReturnId()),
                logDetails
        );

        return returnTransactionMapper.toDto(savedReturnTx);
    }

    @Transactional
    public void updateCustomerTransaction(CustomerTransaction transaction, List<ReturnDetailRequest> returnDetails) {
        int netQuantityChange = 0;
        BigDecimal netAmountChange = BigDecimal.ZERO;

        for (ReturnDetailRequest detailReq : returnDetails) {
            Long productId = detailReq.getProductId();
            Long variantId = detailReq.getVariantId();
            int qtyReturned = detailReq.getQuantity();

            // Find matching detail in the transaction
            for (CustomerTransactionDetail detail : transaction.getTransactionDetails()) {
                if (
                        detail.getProduct().getId().equals(productId) &&
                                detail.getVariant().getId().equals(variantId)
                ) {
                    if (detail.getQuantityReturned() == null) detail.setQuantityReturned(0);
                    detail.setQuantityReturned(detail.getQuantityReturned() + qtyReturned);

                    detail.setQuantity(detail.getQuantity() - qtyReturned);
                    netQuantityChange += qtyReturned;
                    BigDecimal unitPrice = detail.getUnitPrice() != null ? detail.getUnitPrice() : BigDecimal.ZERO;
                    BigDecimal weight = BigDecimal.valueOf(detail.getVariant().getWeight());
                    BigDecimal linePrice = weight.multiply(unitPrice).multiply(BigDecimal.valueOf(qtyReturned));
                    netAmountChange = netAmountChange.add(linePrice);
                    break;
                }
            }
        }

        transaction.setTotalQuantity(transaction.getTotalQuantity() - netQuantityChange);
        transaction.setTotalAmount(transaction.getTotalAmount().subtract(netAmountChange));
        transaction.setHasReturned(true);

        customerTransactionRepository.save(transaction);
    }

}
