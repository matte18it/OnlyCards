package unical.enterpriceapplication.onlycards.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;


import unical.enterpriceapplication.onlycards.application.data.entities.cards.Condition;

@Data
public class ProductEditDto {
    private Condition condition;
    
    @Size(max = 255, min = 3, message = "The title must be between 3 and 255 characters.")
    private String stateDescription;
    @Valid
    private MoneyDto price;
    @Valid
    private List<ProductImageDto> images = new ArrayList<>();
}
