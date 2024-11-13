package unical.enterpriceapplication.onlycards.application.data.entities.accounts;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Product;

import java.util.List;
import java.util.UUID;

@Getter @Setter
@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false, columnDefinition="uuid")
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToMany
    @JoinTable(name = "cart_product",
            joinColumns = @JoinColumn(name = "cart_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;
}

