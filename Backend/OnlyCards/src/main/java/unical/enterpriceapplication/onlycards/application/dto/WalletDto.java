package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Currency;

import java.util.List;

@Data
public class WalletDto {
    @NotNull @Min(0) @Max(1000000)
    private double balance;
    @NotNull
    private Currency currency;
    @NotNull
    private List<TransactionsDto> transactions;
    @NotNull @Min(0)
    private long totalTransactions;
    @NotNull @Min(0)
    private int totalPages;
}
