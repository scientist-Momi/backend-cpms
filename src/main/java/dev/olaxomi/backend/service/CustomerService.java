package dev.olaxomi.backend.service;

import dev.olaxomi.backend.dto.CustomerDto;
import dev.olaxomi.backend.enums.Status;
import dev.olaxomi.backend.mapper.CustomerMapper;
import dev.olaxomi.backend.model.Customer;
import dev.olaxomi.backend.model.CustomerWallet;
import dev.olaxomi.backend.repository.CustomerRepository;
import dev.olaxomi.backend.request.NewCustomerRequest;
import dev.olaxomi.backend.request.UpdateCustomerRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public List<CustomerDto> allCustomers(){
        List<Customer> initCustomers = customerRepository.findAllOrderByCreatedAtDesc();
        return customerMapper.toDtoList(initCustomers);
    }

    public CustomerDto getCustomerById(UUID customerId){
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found!"));
        return customerMapper.toDto(customer);
    }

    public CustomerDto getCustomerByName(String customerName){
        Customer customer = customerRepository.findByName(customerName)
                .orElseThrow(() -> new RuntimeException("Customer not found!"));
        return customerMapper.toDto(customer);
    }

    @Transactional
    public CustomerDto newCustomer(NewCustomerRequest request){
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone already exists");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setAlias(request.getAlias());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setStatus(Status.ACTIVE);
        customer.setCustomerType(request.getCustomerType());
        customer.setCreditLimit(BigDecimal.ZERO);
        customer.setCustomerNotes(request.getCustomerNotes());

        CustomerWallet wallet = new CustomerWallet();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCustomer(customer);
        customer.setCustomerWallet(wallet);

        return customerMapper.toDto(customerRepository.save(customer));
    }

    @Transactional
    public CustomerDto updateCustomer(UpdateCustomerRequest request, UUID customerId){
        return customerRepository.findById(customerId)
                .map(existingCustomer -> {
                    if (request.getName() != null) existingCustomer.setName(request.getName());
                    if (request.getAlias() != null) existingCustomer.setAlias(request.getAlias());
                    if (request.getEmail() != null) existingCustomer.setEmail(request.getEmail());
                    if (request.getPhone() != null) existingCustomer.setPhone(request.getPhone());
                    if (request.getAddress() != null) existingCustomer.setAddress(request.getAddress());
                    if (request.getStatus() != null) existingCustomer.setStatus(request.getStatus());
                    if (request.getCustomerType() != null) existingCustomer.setCustomerType(request.getCustomerType());
                    if (request.getCreditLimit() != null) existingCustomer.setCreditLimit(request.getCreditLimit());
                    if (request.getCustomerNotes() != null) existingCustomer.setCustomerNotes(request.getCustomerNotes());
                    return customerMapper.toDto(customerRepository.save(existingCustomer));
                })
                .orElseThrow(() -> new EntityNotFoundException("Customer not found!"));
    }

    public List<CustomerDto> searchCustomersByName(String query) {
        if (query == null || query.trim().isEmpty() || query.length() < 3) {
            throw new IllegalArgumentException("Query must be at least 3 characters long.");
        }

        // Fetch results (e.g., top 10 matches)
        Page<Customer> customers = customerRepository.findByNameStartingWithIgnoreCase(
                query.trim(),
                PageRequest.of(0, 10)
        );

        return customerMapper.toDtoList(customers.getContent());
    }
}
