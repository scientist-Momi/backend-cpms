package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.CustomerWalletDto;
import dev.olaxomi.backend.enums.TransactionType;
import dev.olaxomi.backend.exception.InsufficientFundsException;
import dev.olaxomi.backend.mapper.CustomerWalletMapper;
import dev.olaxomi.backend.model.Customer;
import dev.olaxomi.backend.model.CustomerWallet;
import dev.olaxomi.backend.model.WalletTransaction;
import dev.olaxomi.backend.repository.CustomerRepository;
import dev.olaxomi.backend.repository.CustomerWalletRepository;
import dev.olaxomi.backend.repository.WalletTransactionRepository;
import dev.olaxomi.backend.request.CustomerDebtAdjustmentRequest;
import dev.olaxomi.backend.request.CustomerWithdrawRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerWalletService {
    private final CustomerWalletRepository walletRepository;
    private final CustomerWalletMapper walletMapper;
    private final CustomerRepository customerRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public CustomerWalletService(CustomerWalletRepository walletRepository, CustomerWalletMapper walletMapper, CustomerRepository customerRepository, WalletTransactionRepository walletTransactionRepository) {
        this.walletRepository = walletRepository;
        this.walletMapper = walletMapper;
        this.customerRepository = customerRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    public List<CustomerWalletDto> allWallets(){
        List<CustomerWallet> customerWallets = new ArrayList<>();
        walletRepository.findAll().forEach(customerWallets::add);
        return walletMapper.toDtoList(customerWallets);
    }

    @Transactional
    public CustomerWalletDto processWithdraw(CustomerWithdrawRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        CustomerWallet wallet = customer.getCustomerWallet();
        if (wallet == null) throw new IllegalStateException("Customer does not have a wallet.");

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Withdrawal amount exceeds available balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(wallet);

        WalletTransaction walletTx = new WalletTransaction();
        walletTx.setWallet(wallet);
        walletTx.setAmount(request.getAmount().negate());
        walletTx.setBalanceAfterTransaction(wallet.getBalance());
        walletTx.setTransactionType(TransactionType.WITHDRAWAL);
        walletTx.setReference(request.getNote() != null ? request.getNote() : "Withdrawal on " + LocalDateTime.now());
        walletTransactionRepository.save(walletTx);

        return walletMapper.toDto(wallet);
    }

    @Transactional
    public CustomerWalletDto addHistoricalDebt(CustomerDebtAdjustmentRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        CustomerWallet wallet = customer.getCustomerWallet();
        if (wallet == null) throw new IllegalStateException("Customer does not have a wallet.");

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debt amount must be greater than zero");
        }

        BigDecimal newBalance = wallet.getBalance().subtract(request.getAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        WalletTransaction adjustmentTx = new WalletTransaction();
        adjustmentTx.setWallet(wallet);
        adjustmentTx.setAmount(request.getAmount().negate());
        adjustmentTx.setBalanceAfterTransaction(newBalance);
        adjustmentTx.setTransactionType(TransactionType.ADMIN_ADJUSTMENT);
        adjustmentTx.setReference("[DEBT ADDITION] " + request.getNote());
        adjustmentTx.setDescription("Added historical debt: " + request.getAmount());
        walletTransactionRepository.save(adjustmentTx);

        return walletMapper.toDto(wallet);
    }

}
