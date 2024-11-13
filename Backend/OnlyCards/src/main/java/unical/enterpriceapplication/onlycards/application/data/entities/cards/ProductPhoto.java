package unical.enterpriceapplication.onlycards.application.data.entities.cards;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "product_photo")
@Getter
@Setter
public class ProductPhoto {
    @Id
    @Getter
    @Column(unique = true, nullable = false, updatable = false, columnDefinition="uuid")
    private UUID id;
    @Column(nullable = false)
    private String photo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @JsonIgnore
    private Product product;
}
