package dev.olaxomi.backend.controller;

import dev.olaxomi.backend.dto.CustomerWalletDto;
import dev.olaxomi.backend.request.CustomerDebtAdjustmentRequest;
import dev.olaxomi.backend.request.CustomerWithdrawRequest;
import dev.olaxomi.backend.response.MessageResponse;
import dev.olaxomi.backend.service.CustomerWalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequestMapping("/v1/customer/wallet")
@RestController
public class CustomerWalletController {
    private final CustomerWalletService walletService;

    public CustomerWalletController(CustomerWalletService walletService) {
        this.walletService = walletService;
    }

    @PreAuthorize("hasAuthority('VIEW_WALLET')")
    @GetMapping("/all")
    public ResponseEntity<MessageResponse> allWallets(){
        List<CustomerWalletDto> wallets = walletService.allWallets();
        return ResponseEntity.ok(new MessageResponse("success", wallets));
    }

    @PreAuthorize("hasAuthority('UPDATE_WALLET')")
    @PostMapping("/withdraw")
    public ResponseEntity<MessageResponse> withdraw(@RequestBody CustomerWithdrawRequest request){
        try{
            CustomerWalletDto wallet = walletService.processWithdraw(request);
            return ResponseEntity.ok(new MessageResponse("success", wallet));
        }catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAuthority('UPDATE_WALLET')")
    @PostMapping("/debt/add")
    public ResponseEntity<MessageResponse> addDebt(@RequestBody CustomerDebtAdjustmentRequest request){
        try{
            CustomerWalletDto wallet = walletService.addHistoricalDebt(request);
            return ResponseEntity.ok(new MessageResponse("success", wallet));
        }catch (RuntimeException e) {
            return ResponseEntity.status(NOT_FOUND).body(new MessageResponse(e.getMessage(), null));
        }
    }
}
