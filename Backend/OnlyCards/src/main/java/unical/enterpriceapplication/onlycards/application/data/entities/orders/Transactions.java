package unical.enterpriceapplication.onlycards.application.data.entities.orders;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Wallet;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Product;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter
public class Transactions {
    @Id
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false, columnDefinition="uuid")
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private Money value;

    @Column(nullable = false)
    private boolean type;   // 0 = debit, 1 = credit

    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id", referencedColumnName = "id")
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "orders_id", referencedColumnName = "id")
    private Orders orders;
}
