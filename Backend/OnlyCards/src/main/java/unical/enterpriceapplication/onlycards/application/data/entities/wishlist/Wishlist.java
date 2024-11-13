package unical.enterpriceapplication.onlycards.application.data.entities.wishlist;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Wishlist {
    @Id
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false, columnDefinition="uuid")
    @Getter
    private UUID id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "wishlist", orphanRemoval = true)
    List<UserWishlist> users;
    @Column(nullable = false)
    private Boolean isPublic = false;

    @ManyToMany
    @JoinTable(
            name = "wishlist_product",
            joinColumns = @JoinColumn(name = "wishlist_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Product> products;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "wishlist_id", referencedColumnName = "id")
    private CapabilityToken token;


    @Column(nullable = false)
    private LocalDateTime lastUpdate = LocalDateTime.now();
    @PrePersist
    public void prePersist() {
        lastUpdate = LocalDateTime.now();
    }
    @PreUpdate
    public void preUpdate() {
        lastUpdate = LocalDateTime.now();
    }
    @PreRemove
    private void preRemove() {
        // Rimuove la relazione con gli utenti
        for (UserWishlist userWishlist : users) {
            userWishlist.setWishlist(null);
        }
        users.clear();

        // Rimuove la relazione con i prodotti
        products.clear();

        // Rimuove il token di capacità associato
        if (token != null) {
            token = null;
        }
    }
}
