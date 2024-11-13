package unical.enterpriceapplication.onlycards.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Condition;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductPhoto;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto {
    @NotNull
    private UUID id;
    @NotBlank
    @Size(min = 1, max = 200)
    private String stateDescription;
    @NotNull
    private LocalDate releaseDate;
    @NotNull
    private List<ProductPhoto> images;
    @NotNull
    private Money price;
    @NotNull
    private ProductTypeDto productType;
    @NotNull
    private AccountDto account;
    @NotNull
    private Condition condition;
    private boolean sold;
}
