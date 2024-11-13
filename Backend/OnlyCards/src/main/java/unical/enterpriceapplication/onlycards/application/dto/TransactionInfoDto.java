package unical.enterpriceapplication.onlycards.application.dto;


import lombok.Data;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;

@Data
public class TransactionInfoDto {
    Money value;
    String productPhoto;
    String productName;

}
