package dev.olaxomi.backend.mapper;

import dev.olaxomi.backend.dto.CustomerTransactionDetailDto;
import dev.olaxomi.backend.dto.CustomerTransactionDto;
import dev.olaxomi.backend.dto.ReturnTransactionDetailDto;
import dev.olaxomi.backend.dto.ReturnTransactionDto;
import dev.olaxomi.backend.model.CustomerTransaction;
import dev.olaxomi.backend.model.CustomerTransactionDetail;
import dev.olaxomi.backend.model.ReturnTransaction;
import dev.olaxomi.backend.model.ReturnTransactionDetail;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReturnTransactionMapper {
    @Autowired
    private ModelMapper modelMapper;

    public ReturnTransactionDto toDto(ReturnTransaction transaction) {
        if (transaction == null) return null;
        ReturnTransactionDto dto = modelMapper.map(transaction, ReturnTransactionDto.class);

        if (transaction.getReturnDetails() != null) {
            List<ReturnTransactionDetailDto> detailDtos = transaction.getReturnDetails()
                    .stream()
                    .map(this::toDetailDto)
                    .collect(Collectors.toList());
            dto.setReturnDetails(detailDtos);
        }

        if (transaction.getCustomer() != null) {
            dto.setCustomerId(transaction.getCustomer().getCustomerId());
            dto.setCustomerName(transaction.getCustomer().getName());
        }

        return dto;
    }

    public ReturnTransactionDetailDto toDetailDto(ReturnTransactionDetail detail) {
        if (detail == null) return null;
        ReturnTransactionDetailDto dto = modelMapper.map(detail, ReturnTransactionDetailDto.class);

        if (detail.getProduct() != null) {
            dto.setProductId(detail.getProduct().getId());
        }

        return dto;
    }

    public List<ReturnTransactionDto> toDtoList(List<ReturnTransaction> transactions) {
        return transactions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ReturnTransaction fromDto(ReturnTransactionDto dto) {
        return modelMapper.map(dto, ReturnTransaction.class);
    }
}
