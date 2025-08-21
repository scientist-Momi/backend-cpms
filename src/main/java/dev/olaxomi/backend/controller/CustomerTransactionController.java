package dev.olaxomi.backend.controller;

import dev.olaxomi.backend.dto.CustomerTransactionDto;
import dev.olaxomi.backend.dto.ReturnTransactionDto;
import dev.olaxomi.backend.request.NewCustomerTransactionRequest;
import dev.olaxomi.backend.request.ReturnRequest;
import dev.olaxomi.backend.response.MessageResponse;
import dev.olaxomi.backend.service.CustomerTransactionService;
import dev.olaxomi.backend.service.ReturnService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequestMapping("/v1/customer/transaction")
@RestController
public class CustomerTransactionController {
    private final CustomerTransactionService transactionService;
    private final ReturnService returnService;

    public CustomerTransactionController(CustomerTransactionService transactionService, ReturnService returnService) {
        this.transactionService = transactionService;
        this.returnService = returnService;
    }

    @PreAuthorize("hasAuthority('VIEW_TRANSACTION')")
    @GetMapping("/all")
    public ResponseEntity<MessageResponse> all(){
        List<CustomerTransactionDto> transactions = transactionService.allTransactions();
        return ResponseEntity.ok(new MessageResponse("success", transactions));
    }

    @PreAuthorize("hasAuthority('VIEW_TRANSACTION')")
    @GetMapping("/{customerId}")
    public ResponseEntity<MessageResponse> getTransaction(@PathVariable UUID customerId){
        List<CustomerTransactionDto> transactions = transactionService.getTransactionsForCustomer(customerId);
        return ResponseEntity.ok(new MessageResponse("success", transactions));
    }

    @PreAuthorize("hasAuthority('VIEW_TRANSACTION')")
    @GetMapping("/t/{transactionId}")
    public ResponseEntity<MessageResponse> getTransactionById(@PathVariable Long transactionId){
        CustomerTransactionDto transaction = transactionService.getTransaction(transactionId);
        return ResponseEntity.ok(new MessageResponse("success", transaction));
    }

    @PreAuthorize("hasAuthority('CREATE_TRANSACTION')")
    @PostMapping("/new")
    public ResponseEntity<MessageResponse> newTransaction(@RequestBody NewCustomerTransactionRequest request){
        try{
            CustomerTransactionDto transaction = transactionService.addCustomerTransaction(request);
            return ResponseEntity.ok(new MessageResponse("success", transaction));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_ACCEPTABLE).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('CREATE_TRANSACTION')")
    @PostMapping("/return")
    public ResponseEntity<MessageResponse> newTransaction(@RequestBody ReturnRequest request){
        try{
            ReturnTransactionDto transaction = returnService.processReturn(request);
            return ResponseEntity.ok(new MessageResponse("success", transaction));
        } catch (RuntimeException e) {
            return ResponseEntity.status(NOT_ACCEPTABLE).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('VIEW_TRANSACTION')")
    @GetMapping("/product/{productId}")
    public ResponseEntity<MessageResponse> getByProduct(@PathVariable Long productId){
        List<CustomerTransactionDto> transactions = transactionService.getTransactionsByProduct(productId);
        return ResponseEntity.ok(new MessageResponse("success", transactions));
    }

    @PreAuthorize("hasAuthority('VIEW_TRANSACTION')")
    @GetMapping("/customer/{customerId}/product/{productId}")
    public ResponseEntity<MessageResponse> getByCustomerAndProduct(@PathVariable UUID customerId, @PathVariable Long productId) {
        List<CustomerTransactionDto> transactions = transactionService.getTransactionsByCustomerAndProduct(customerId, productId);
        return ResponseEntity.ok(new MessageResponse("success", transactions));
    }
}
