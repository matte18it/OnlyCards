package unical.enterpriceapplication.onlycards.application.data.entities.accounts;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Transactions;

import java.util.List;
import java.util.UUID;

@Entity
@Getter @Setter
public class Wallet {
    @Id
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false, columnDefinition="uuid")
    private UUID id;

    @Column(nullable = false)
    private Money balance;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transactions> transactions;

    @PreRemove
    private void preRemove() {
        // Rimuove tutte le transazioni associate al wallet
        for (Transactions transaction : transactions) {
            transaction.setWallet(null);
        }
    }

}
