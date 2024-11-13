package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionsDto {
    @NotNull
    private UUID id;
    @NotNull
    private LocalDateTime date;
    @NotNull
    private Money value;
    @NotNull
    private boolean type;
    @NotNull
    private UUID walletID;
    @NotNull
    private ProductDto product;
}
