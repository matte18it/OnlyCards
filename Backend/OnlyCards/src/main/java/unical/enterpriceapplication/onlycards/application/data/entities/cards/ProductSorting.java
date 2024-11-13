package unical.enterpriceapplication.onlycards.application.data.entities.cards;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public enum ProductSorting {
    POPOLARITY("popolarity", "Popolarità"),
    PRICE_ASC("price asc", "Prezzo Ascendente"),
    PRICE_DESC("price desc", "Prezzo Discendente");

    private final String key;
    private final String value;

    ProductSorting(String key, String value) {
        this.key = key;
        this.value = value;
    }
    public static Map<String, String> getSortingOptions() {
        return Arrays.stream(ProductSorting.values())
                .collect(Collectors.toMap(ProductSorting::getKey, ProductSorting::getValue));
    }
    public static Optional<ProductSorting> fromKey(String key) {
        return Arrays.stream(ProductSorting.values())
                .filter(e -> e.getKey().equals(key))
                .findFirst();
    }

}
