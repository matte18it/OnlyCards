package unical.enterpriceapplication.onlycards.application.data.entities.cards;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "FEATURE_PRODUCT")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"product_type_id", "feature_id"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeatureProduct {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_type_id", referencedColumnName = "id")
    private ProductType productType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "feature_id", referencedColumnName = "id")
    private Feature feature;

    @Column(nullable = false)
    private String value;
}
