package unical.enterpriceapplication.onlycards.application.data.entities.wishlist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
public class CapabilityToken {
    @Id
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false, columnDefinition="uuid")
    @Getter
    private UUID id;

    @Column( unique = true, nullable = false)
    private String token;

    @OneToOne(mappedBy = "token", optional = false)
    private Wishlist wishlist;
}
