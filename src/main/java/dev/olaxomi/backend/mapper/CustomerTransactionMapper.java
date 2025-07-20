package dev.olaxomi.backend.mapper;

import dev.olaxomi.backend.dto.CustomerTransactionDetailDto;
import dev.olaxomi.backend.dto.CustomerTransactionDto;
import dev.olaxomi.backend.model.CustomerTransaction;
import dev.olaxomi.backend.model.CustomerTransactionDetail;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerTransactionMapper {
    @Autowired
    private ModelMapper modelMapper;

    public CustomerTransactionDto toDto(CustomerTransaction transaction) {
        if (transaction == null) return null;
        CustomerTransactionDto dto = modelMapper.map(transaction, CustomerTransactionDto.class);

        // Manually map nested transactionDetails if not automatically handled
        if (transaction.getTransactionDetails() != null) {
            List<CustomerTransactionDetailDto> detailDtos = transaction.getTransactionDetails()
                    .stream()
                    .map(this::toDetailDto)
                    .collect(Collectors.toList());
            dto.setTransactionDetails(detailDtos);
        }

        // Set customerId explicitly if not mapped automatically
        if (transaction.getCustomer() != null) {
            dto.setCustomerId(transaction.getCustomer().getCustomerId());
        }

        return dto;
    }

    public CustomerTransactionDetailDto toDetailDto(CustomerTransactionDetail detail) {
        if (detail == null) return null;
        CustomerTransactionDetailDto dto = modelMapper.map(detail, CustomerTransactionDetailDto.class);

        // Set productId explicitly if not mapped automatically
        if (detail.getProduct() != null) {
            dto.setProductId(detail.getProduct().getId());
        }

        return dto;
    }

    public List<CustomerTransactionDto> toDtoList(List<CustomerTransaction> transactions) {
        return transactions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CustomerTransaction fromDto(CustomerTransactionDto dto) {
        return modelMapper.map(dto, CustomerTransaction.class);
    }
}
