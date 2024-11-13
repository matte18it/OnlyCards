package unical.enterpriceapplication.onlycards.application.data.entities.accounts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
import lombok.Getter;
import lombok.Setter;
import unical.enterpriceapplication.onlycards.application.data.entities.InvalidatedToken;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Product;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Orders;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Status;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.UserWishlist;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.Wishlist;

@Entity
@Getter @Setter
public class User {
    @Id
    @UuidGenerator
    @Getter
    @Column(unique = true, nullable = false, updatable = false, columnDefinition="uuid")
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column
    private String password;

    @Column(name = "cellphone_number")
    private String cellphoneNumber;

    @Column(nullable = false)
    private Boolean blocked=false;
    @Column(nullable = false)
    private Boolean oauthUser=false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Wallet wallet;

    @OneToMany(mappedBy = "user",  cascade = CascadeType.ALL)
    private List<InvalidatedToken> invalidatedTokens;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<UserWishlist> userWishlists;

    @OneToMany(mappedBy = "user")
    private List<Orders> orders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

    @PreRemove
    private void preRemove() {
        // Dissociazione dei prodotti (venduti o meno)
        for (Product product : products) {
            product.setUser(null);  // Solo dissocia i prodotti dall'utente
        }

        // Gestione delle wishlist - dissocia solamente
        for (UserWishlist userWishlist : userWishlists) {
            Wishlist wishlist = userWishlist.getWishlist();
            if (wishlist != null) {
                userWishlist.setUser(null);  // Dissocia l'utente dalla wishlist
            }
        }

        // Dissocia il carrello
        if (cart != null) {
            cart.setUser(null);  // Dissocia il carrello dall'utente
        }

        if (orders != null) {
            for (Orders order : orders) {
                if (order.getStatus() == Status.SHIPPED || order.getStatus() == Status.DELIVERED) {
                    order.setUser(null);  // Dissocia l'utente dagli ordini
                }
            }
        }
    }
}
