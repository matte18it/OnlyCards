package unical.enterpriceapplication.onlycards.application.data.entities.cards;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Cart;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Transactions;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.Wishlist;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Product {

    @Id
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false, columnDefinition="uuid")
    private UUID id;

    @Column(name = "state_description", length = 512, nullable = false)
    private String stateDescription;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name="sold", nullable = false)
    private boolean sold;

    @OneToMany(mappedBy = "product", orphanRemoval = true)
    private List<ProductPhoto> images;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "amount", column = @Column(name = "price_amount")),
            @AttributeOverride( name = "currency", column = @Column(name = "price_currency")),
    })
    private Money price;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_type_id", referencedColumnName = "id")
    private ProductType productType;

    @Enumerated
    @Column(name = "product_condition")
    private Condition condition;

    @ManyToMany(mappedBy = "products")
    private List<Cart> carts;


    @ManyToMany(mappedBy = "products")
    private Set<Wishlist> wishlists;

    @ManyToOne()
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany
    private List<Transactions> transactions;

    @PreRemove
    public void preRemove() {
        wishlists.stream()
         .forEach(wishlist -> wishlist.getProducts().remove(this));

        carts.stream()
            .forEach(cart -> cart.getProducts().remove(this));

        if (this.user != null) {
            this.user.getProducts().remove(this);
            this.user = null;
        }
    }
}
