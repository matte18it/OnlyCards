package unical.enterpriceapplication.onlycards.application.data.entities.cards;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Embeddable
@Setter
public class Money {
    private Double amount= 0.0;
    private Currency currency = Currency.EUR;
}
