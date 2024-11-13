package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Condition;

import java.util.ArrayList;
import java.util.List;

@Data
public class SaveProductDto {
    @NotNull
    String description;
    @NotNull
    Condition condition;
    @NotNull
    MoneyDto price;
    @NotNull
    String productType;
    @NotNull
    List<ProductImageDto> images = new ArrayList<>();
}
