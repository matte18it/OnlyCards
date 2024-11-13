package unical.enterpriceapplication.onlycards.application.data.entities.cards;

import lombok.Getter;

@Getter
public enum Currency {
    EUR("€");

    private final String symbol;

    Currency(String symbol) {
        this.symbol = symbol;
    }
}
