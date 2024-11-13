package unical.enterpriceapplication.onlycards.application.data.entities.wishlist;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;

@Entity
@Table(name = "account_wishlist")
@Getter
@Setter
public class UserWishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;
    @Enumerated(EnumType.STRING)
    private WishlistOwnership ownership;

    @ManyToOne
    @JoinColumn(name = "wishlist_id")
    private Wishlist wishlist;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
