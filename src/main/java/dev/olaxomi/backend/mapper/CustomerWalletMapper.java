package dev.olaxomi.backend.mapper;

import dev.olaxomi.backend.dto.CustomerWalletDto;
import dev.olaxomi.backend.dto.WalletTransactionDto;
import dev.olaxomi.backend.model.CustomerWallet;
import dev.olaxomi.backend.model.WalletTransaction;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerWalletMapper {
    @Autowired
    private ModelMapper modelMapper;

    public CustomerWalletDto toDto(CustomerWallet wallet) {
        if (wallet == null) return null;
        CustomerWalletDto dto = modelMapper.map(wallet, CustomerWalletDto.class);

        // Map wallet transactions if present
        if (wallet.getTransactions() != null) {
            List<WalletTransactionDto> txDtos = wallet.getTransactions()
                    .stream()
                    .sorted(Comparator.comparing(WalletTransaction::getCreatedAt).reversed())
                    .map(this::toTransactionDto)
                    .collect(Collectors.toList());
            dto.setTransactions(txDtos);

        }

        return dto;
    }

    public WalletTransactionDto toTransactionDto(WalletTransaction transaction) {
        if (transaction == null) return null;
        WalletTransactionDto dto = modelMapper.map(transaction, WalletTransactionDto.class);
        // No wallet field set to prevent recursion
        return dto;
    }

    public List<CustomerWalletDto> toDtoList(List<CustomerWallet> wallets) {
        return wallets.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CustomerWallet fromDto(CustomerWalletDto dto) {
        return modelMapper.map(dto, CustomerWallet.class);
    }
}
