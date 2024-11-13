package unical.enterpriceapplication.onlycards.application.data.entities.cards;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ProductType {
     @Id
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false, columnDefinition="uuid")
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String language;

    @Column
    private String photo;

    @Column(name = "num_sell")
    private Integer numSell=0;

    @Column(name = "last_add")
    private LocalDate lastAdd= LocalDate.now();

    @OneToMany(mappedBy = "productType" , fetch = FetchType.LAZY)
    private List<Product> products;

    @Embedded@AttributeOverrides({
            @AttributeOverride( name = "amount", column = @Column(name = "min_price_amount")),
            @AttributeOverride( name = "currency", column = @Column(name = "min_price_currency")),
    })
    @Column(nullable = false)
    private Money minPrice= new Money(0D, Currency.EUR);

    @Column(nullable = false)
    private String game;
    @Column(nullable = false)
    private String type;

    @OneToMany(mappedBy = "productType", orphanRemoval = true, cascade = CascadeType.PERSIST)
    private List<FeatureProduct> features=new ArrayList<>();
 
}
