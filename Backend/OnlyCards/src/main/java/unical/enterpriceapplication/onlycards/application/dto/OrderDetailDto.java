package unical.enterpriceapplication.onlycards.application.dto;


import lombok.Data;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Status;

import java.util.List;

import java.time.LocalDateTime;
import java.util.ArrayList;


@Data
public class OrderDetailDto {
    Status status;
    String userLastEdit;
    LocalDateTime modifyDate;
    List<TransactionInfoDto> transactions = new ArrayList<>();

}
