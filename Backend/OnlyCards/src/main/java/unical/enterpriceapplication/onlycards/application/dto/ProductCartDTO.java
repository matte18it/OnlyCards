package unical.enterpriceapplication.onlycards.application.dto;

import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.NoArgsConstructor;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductPhoto;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Condition;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ProductCartDTO {
    private UUID id;
    @Expose
    private String cardName;
    @Expose
    private String cardLanguage;
    @Expose
    private Money price;
    @Expose
    private String game;
    @Expose
    private String type;
    private String stateDescription;
    private LocalDate releaseDate;
    private List<ProductPhoto> images;
    private Condition condition;
    private boolean sold;
    private String username;
    private String email;
    private String cellphone;
    private Integer numSell;

}
