package unical.enterpriceapplication.onlycards.application.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Wallet;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    @Query("SELECT w FROM Wallet w")
    List<Wallet> getAllWallets();
}
