package unical.enterpriceapplication.onlycards.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;



import lombok.Data;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;

@Data
public class ProductInfoDto {
    private UUID id;

    private LocalDate releaseDate;

    private List<String> images;

    private Money price;

    private String name;

    private String language;

    private String game;

   
    private String condition;
    
}
