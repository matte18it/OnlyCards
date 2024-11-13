package unical.enterpriceapplication.onlycards.application.data.entities.wishlist;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
@Getter
public enum WishlistOwnership {
    OWNER("owner", "Proprietario"),
    SHARED_WITH("shared_with", "Condivisa");
    private final String key;
    private final String value;

    WishlistOwnership(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static Optional<WishlistOwnership> fromKey(String keyOwnership) {
        return Arrays.stream(WishlistOwnership.values())
                .filter(ownership -> ownership.getKey().equals(keyOwnership))
                .findFirst();
    }
}
