package unical.enterpriceapplication.onlycards.application.data.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Wallet;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Transactions;
import unical.enterpriceapplication.onlycards.application.data.repository.TransactionsRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.UserRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.WalletRepository;
import unical.enterpriceapplication.onlycards.application.dto.TransactionsDto;
import unical.enterpriceapplication.onlycards.application.dto.WalletDto;
import unical.enterpriceapplication.onlycards.application.exception.LimitExceedException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;


@Service
@Slf4j
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionsRepository transactionsRepository;

    @Override
    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    @Override
    public WalletDto getWallet(UUID userID, Pageable pageable) throws ResourceNotFoundException {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException(userID.toString(), "User not found"));

        if(user.getWallet() == null)
            throw new ResourceNotFoundException(userID.toString(),"wallet not found");

        WalletDto dto = new WalletDto();
        dto.setBalance(user.getWallet().getBalance().getAmount());
        dto.setCurrency(user.getWallet().getBalance().getCurrency());

        Page<Transactions> transactionsPage = transactionsRepository
                .findByWalletIdAndProductId(user.getWallet().getId(),null, pageable);

        Page<TransactionsDto> transactionsDtoPage = transactionsPage
                .map(this::transactionToDTO);

        dto.setTransactions(transactionsDtoPage.getContent());
        dto.setTotalTransactions(transactionsPage.getTotalElements());
        dto.setTotalPages(transactionsPage.getTotalPages());

        log.debug("User wallet: {}", dto.getBalance());

        return dto;
    }

    private TransactionsDto transactionToDTO(Transactions transaction) {
        TransactionsDto dto = new TransactionsDto();

        dto.setId(transaction.getId());
        dto.setWalletID(transaction.getWallet().getId());
        dto.setType(transaction.isType());
        dto.setValue(transaction.getValue());
        dto.setDate(transaction.getDate());

        return dto;
    }

    @Override
    @Transactional(rollbackFor = {ResourceNotFoundException.class, LimitExceedException.class, Exception.class})
    public void rechargeWallet(UUID userID, Money amount) throws ResourceNotFoundException, LimitExceedException {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException(userID.toString(), "User not found"));

        Wallet wallet = user.getWallet();

        if (amount.getAmount() <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        if(wallet.getBalance().getAmount() + amount.getAmount() > MAX_AMOUNT_WALLET)
            throw  new LimitExceedException("exceeded", (int) (wallet.getBalance().getAmount() + amount.getAmount()));

        Money newBalance = new Money();
        newBalance.setCurrency(wallet.getBalance().getCurrency());
        BigDecimal actual = new BigDecimal(Double.toString(wallet.getBalance().getAmount()));
        BigDecimal incr = new BigDecimal(Double.toString(amount.getAmount()));
        BigDecimal res = actual.add(incr).setScale(2, RoundingMode.DOWN);
        newBalance.setAmount(res.doubleValue());
        wallet.setBalance(newBalance);

        walletRepository.save(wallet);
        log.debug("wallet {} recharged with {} {}", wallet.getId(), amount.getAmount(), amount.getCurrency());

        Transactions transaction = new Transactions();
        transaction.setDate(LocalDateTime.now());
        transaction.setValue(amount);
        transaction.setType(true); // true -> credito
        transaction.setWallet(wallet);
        transaction.setProduct(null);
        transaction.setOrders(null);
        transactionsRepository.save(transaction);

    }

    @Override
    @Transactional(rollbackFor = {ResourceNotFoundException.class, LimitExceedException.class, Exception.class})
    public void withdrawFromWallet(UUID userID, Money amount) throws LimitExceedException, ResourceNotFoundException {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException(userID.toString(), "User not found"));

        Wallet wallet = user.getWallet();

        if (amount.getAmount() <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        if (wallet.getBalance().getAmount() - amount.getAmount() < 0)
            throw new LimitExceedException("exceeded", (int) (wallet.getBalance().getAmount() - amount.getAmount()));

        Money newBalance = new Money();
        newBalance.setCurrency(wallet.getBalance().getCurrency());
        BigDecimal actual = new BigDecimal(Double.toString(wallet.getBalance().getAmount()));
        BigDecimal incr = new BigDecimal(Double.toString(amount.getAmount()));
        BigDecimal res = actual.subtract(incr).setScale(2, RoundingMode.DOWN);
        newBalance.setAmount(res.doubleValue());
        wallet.setBalance(newBalance);

        walletRepository.save(wallet);
        log.debug("withdrew {} {} from wallet {}", amount.getAmount(), amount.getCurrency(), wallet.getId());


        Transactions transaction = new Transactions();
        transaction.setDate(LocalDateTime.now());
        transaction.setValue(amount);
        transaction.setType(false); // false -> debito
        transaction.setWallet(wallet);
        transaction.setProduct(null);
        transaction.setOrders(null);
        transactionsRepository.save(transaction);

    }

    @Override
    @Transactional(rollbackFor = {ResourceNotFoundException.class, LimitExceedException.class, Exception.class})
    public void transferAmount(UUID buyerID, UUID sellerID, Money amount) throws ResourceNotFoundException, LimitExceedException {
        User buyer = userRepository.findById(buyerID)
                .orElseThrow(() -> new ResourceNotFoundException(buyerID.toString(), "User not found"));

        User seller = userRepository.findById(sellerID)
                .orElseThrow(() -> new ResourceNotFoundException(sellerID.toString(), "User not found"));

        if (amount.getAmount() <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Wallet buyerWallet = buyer.getWallet();
        Wallet sellerWallet = seller.getWallet();

        if (buyerWallet.getBalance().getAmount() - amount.getAmount() < 0)
            throw new LimitExceedException("exceeded", (int) (buyerWallet.getBalance().getAmount() - amount.getAmount()));

        if(sellerWallet.getBalance().getAmount() + amount.getAmount() > MAX_AMOUNT_WALLET)
            throw  new LimitExceedException("exceeded", (int) (sellerWallet.getBalance().getAmount() + amount.getAmount()));

        Money newBuyerBalance = new Money();
        newBuyerBalance.setCurrency(amount.getCurrency());
        BigDecimal actual = new BigDecimal(Double.toString(buyerWallet.getBalance().getAmount()));
        BigDecimal incr = new BigDecimal(Double.toString(amount.getAmount()));
        BigDecimal res = actual.subtract(incr).setScale(2, RoundingMode.DOWN);
        newBuyerBalance.setAmount(res.doubleValue());
        buyerWallet.setBalance(newBuyerBalance);

        walletRepository.save(buyerWallet);

        Money newSellerBalance = new Money();
        newSellerBalance.setCurrency(amount.getCurrency());
        actual = new BigDecimal(Double.toString(sellerWallet.getBalance().getAmount()));
        incr = new BigDecimal(Double.toString(amount.getAmount()));
        res = actual.add(incr).setScale(2, RoundingMode.DOWN);
        newSellerBalance.setAmount(res.doubleValue());
        sellerWallet.setBalance(newSellerBalance);

        walletRepository.save(sellerWallet);

        log.debug("transferred {} {} from wallet {} to wallet {}", amount.getAmount(), amount.getCurrency(), buyerWallet.getId(), sellerWallet.getId());

    }






}
