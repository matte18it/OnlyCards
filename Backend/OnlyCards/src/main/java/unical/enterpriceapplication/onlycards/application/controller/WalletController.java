package unical.enterpriceapplication.onlycards.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Currency;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;
import unical.enterpriceapplication.onlycards.application.data.service.WalletService;
import unical.enterpriceapplication.onlycards.application.dto.WalletDto;
import unical.enterpriceapplication.onlycards.application.exception.LimitExceedException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;

import org.springframework.data.domain.Pageable;
import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/wallets", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class WalletController {
    private final WalletService walletService;

    @Operation(summary = "Get wallet by user ID", description = "This endpoint returns the wallet of the user with the specified user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/users/{userID}")
    @PreAuthorize("(hasRole('ADMIN') or hasRole('BUYER') or hasRole('SELLER')) and #userID == @authService.getCurrentUserUUID()")
    public ResponseEntity<WalletDto> getWallet(@PathVariable UUID userID,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") @Max(30) int size) throws ResourceNotFoundException {
        Pageable pageable = PageRequest.of(page, size);
        Pageable sortPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "date"));

        return ResponseEntity.ok(walletService.getWallet(userID, sortPageable));
    }


    @Operation(summary = "Recharge wallet", description = "This endpoint recharges the wallet of the user with the specified user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet recharged"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/users/{userId}/recharge")
    @PreAuthorize("(hasRole('BUYER') or hasRole('SELLER')) and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<Void> rechargeWallet(@PathVariable UUID userId, @RequestParam double amount) throws ResourceNotFoundException, LimitExceedException {
        Money money = new Money();
        money.setAmount(amount);
        money.setCurrency(Currency.EUR);

        walletService.rechargeWallet(userId, money);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Withdraw from wallet", description = "This endpoint withdraws from the wallet of the user with the specified user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdrawal successful"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/users/{userId}/withdraw")
    @PreAuthorize("(hasRole('BUYER') or hasRole('SELLER')) and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<Void> withdrawFromWallet(@PathVariable UUID userId, @RequestParam double amount) throws ResourceNotFoundException, LimitExceedException {
            Money money = new Money();
            money.setAmount(amount);
            money.setCurrency(Currency.EUR);

            walletService.withdrawFromWallet(userId, money);
            return ResponseEntity.ok().build();
    }
}
