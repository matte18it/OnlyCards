package unical.enterpriceapplication.onlycards.application.dto;



import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Currency;

@Data
public class MoneyDto {
    @NotNull
    @Positive
    private Double amount;
    @NotNull
    private Currency currency;
}
