package unical.enterpriceapplication.onlycards.application.data.entities.cards;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Feature {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "feature"  )
    private List<FeatureProduct> cards;

}
