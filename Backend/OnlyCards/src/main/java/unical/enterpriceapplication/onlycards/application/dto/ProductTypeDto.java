package unical.enterpriceapplication.onlycards.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.Data;

import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductTypeDto {
    @NotNull
    private UUID id;
    @NotNull
    private Money price;
    @NotBlank
    private String name;
    @NotBlank
    @Size(max = 50, min = 3)
    private String type;
    @Size(max = 50, min = 2)
    @NotBlank
    private String language;
    @NotNull
    @Min(0)
    private Integer numSell;
    @NotNull
    private Money minPrice;
    @NotBlank
    private String photo;
    @Size(max = 50, min = 3)
    @NotBlank
    private String game;
    @NotNull
    private LocalDate lastAdd;
    @NotNull
    private List<FeatureDTO> features;
}
