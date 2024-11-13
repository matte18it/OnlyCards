package unical.enterpriceapplication.onlycards.application.data.entities.cards;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public enum Condition{
    MINT("mint", "Perfetta"),
    NEAR_MINT("near-mint", "Come nuova"),
    EXCELLENT("excellent", "Leggera Usura"),
    GOOD("good", "Usura Visibile"),
    LIGHT_PLAYED("light-played", "Grave Usura"),
    PLAYED("played", "Danneggiata"),
    POOR("poor", "Distrutta");
    private final String key;
    private final String value;

    Condition(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static Map<String, String> getConditions() {
        return Arrays.stream(Condition.values())
                .collect(Collectors.toMap(Condition::getKey, Condition::getValue));
    }
    public static Optional<Condition> fromKey(String key) {
        return Arrays.stream(Condition.values())
                .filter(e -> e.getKey().equals(key))
                .findFirst();
    }
}
