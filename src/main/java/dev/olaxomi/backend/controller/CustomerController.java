package dev.olaxomi.backend.controller;

import dev.olaxomi.backend.dto.CustomerDto;
import dev.olaxomi.backend.request.CustomerDepositRequest;
import dev.olaxomi.backend.request.NewCustomerRequest;
import dev.olaxomi.backend.request.UpdateCustomerRequest;
import dev.olaxomi.backend.response.MessageResponse;
import dev.olaxomi.backend.service.CustomerService;
import dev.olaxomi.backend.service.CustomerTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequestMapping("/v1/customer")
@RestController
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerTransactionService transactionService;

    public CustomerController(CustomerService customerService, CustomerTransactionService transactionService) {
        this.customerService = customerService;
        this.transactionService = transactionService;
    }

    @PreAuthorize("hasAuthority('VIEW_CUSTOMER')")
    @GetMapping("/all")
    public ResponseEntity<MessageResponse> all(){
        List<CustomerDto> customers = customerService.allCustomers();
        return ResponseEntity.ok(new MessageResponse("success", customers));
    }

    @PreAuthorize("hasAuthority('VIEW_CUSTOMER')")
    @GetMapping("/search")
    public List<CustomerDto> searchCustomers(@RequestParam String query) {
        return customerService.searchCustomersByName(query);
    }

    @PreAuthorize("hasAuthority('VIEW_CUSTOMER')")
    @GetMapping("/{customerId}")
    public ResponseEntity<MessageResponse> getCustomer(@PathVariable UUID customerId){
        try{
            CustomerDto customer = customerService.getCustomerById(customerId);
            return ResponseEntity.ok(new MessageResponse("success", customer));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_ACCEPTABLE).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('CREATE_CUSTOMER')")
    @PostMapping("/new")
    public ResponseEntity<MessageResponse> newCustomer(@RequestBody NewCustomerRequest request){
        try{
            CustomerDto customer = customerService.newCustomer(request);
            return ResponseEntity.ok(new MessageResponse("success", customer));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_ACCEPTABLE).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('UPDATE_CUSTOMER')")
    @PutMapping("{customerId}/update")
    public ResponseEntity<MessageResponse> updateCustomer(@PathVariable UUID customerId, @RequestBody UpdateCustomerRequest request){
        try{
            CustomerDto customer = customerService.updateCustomer(request, customerId);
            return ResponseEntity.ok(new MessageResponse("success", customer));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_ACCEPTABLE).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('UPDATE_CUSTOMER')")
    @PostMapping("/wallet/deposit")
    public ResponseEntity<MessageResponse> walletDeposit(@RequestBody CustomerDepositRequest request){
        try{
            CustomerDto customer = transactionService.processDeposit(request);
            return ResponseEntity.ok(new MessageResponse("success", customer));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_ACCEPTABLE).body(new MessageResponse(e.getMessage(), null));
        }
    }
}
