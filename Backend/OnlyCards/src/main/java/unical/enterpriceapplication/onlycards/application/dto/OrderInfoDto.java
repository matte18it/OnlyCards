package unical.enterpriceapplication.onlycards.application.dto;

import java.time.LocalDate;

import java.util.UUID;


import lombok.Data;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Status;
@Data
public class OrderInfoDto {
    UUID id;
    LocalDate addDate;
    String buyer;
    String seller;
    Status status;

}
