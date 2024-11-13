package unical.enterpriceapplication.onlycards.application.data.entities.accounts;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter @Setter
public class Address {
    @Id
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false, columnDefinition="uuid")
    private UUID id;

    @Column(name = "default_address", nullable = false)
    private Boolean defaultAddress=false;
    @Column(name = "weekend_delivery", nullable = false)
    private Boolean weekendDelivery=false;

    @Column(nullable = false)
    private String state;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String street;
    @Column(name = "zip_code", length = 10, nullable = false)
    private String zip;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String surname;
    @Column(name = "telephone_number", length = 15)
    private String telephoneNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
