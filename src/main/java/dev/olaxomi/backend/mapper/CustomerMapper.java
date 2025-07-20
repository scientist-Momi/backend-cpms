package dev.olaxomi.backend.mapper;

import dev.olaxomi.backend.dto.CustomerDto;
import dev.olaxomi.backend.model.Customer;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerMapper {
    @Autowired
    private ModelMapper modelMapper;

    public CustomerDto toDto(Customer customer){
        return modelMapper.map(customer, CustomerDto.class);
    }

    public List<CustomerDto> toDtoList(List<Customer> customers) {
        return customers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    public Customer fromDto(CustomerDto customerDto) {
        return modelMapper.map(customerDto, Customer.class);
    }
}
