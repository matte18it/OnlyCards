package unical.enterpriceapplication.onlycards.application.data.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Wallet;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;
import unical.enterpriceapplication.onlycards.application.dto.WalletDto;
import unical.enterpriceapplication.onlycards.application.exception.LimitExceedException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;

public interface WalletService {
    double MAX_AMOUNT_WALLET = 500000.0;

    Wallet save(Wallet wallet);
    WalletDto getWallet(UUID userID, Pageable pageable) throws ResourceNotFoundException;
    void rechargeWallet(UUID userID, Money amount) throws LimitExceedException, ResourceNotFoundException;
    void withdrawFromWallet(UUID userID, Money amount) throws LimitExceedException,ResourceNotFoundException;
    void transferAmount(UUID buyerID, UUID sellerID, Money amount) throws ResourceNotFoundException, LimitExceedException;
}
