package unical.enterpriceapplication.onlycards.application.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Transactions;

import java.util.UUID;

public interface TransactionsRepository extends JpaRepository<Transactions, UUID> {
    Page<Transactions> findByWalletId(UUID walletId, Pageable pageable);
    Page<Transactions> findByWalletIdAndProductId(UUID walletId, UUID productID, Pageable pageable);

}
