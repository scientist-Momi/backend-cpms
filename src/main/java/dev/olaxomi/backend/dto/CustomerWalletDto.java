package dev.olaxomi.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CustomerWalletDto {
    private Long walletId;
    private BigDecimal balance;
    private List<WalletTransactionDto> transactions;
}
