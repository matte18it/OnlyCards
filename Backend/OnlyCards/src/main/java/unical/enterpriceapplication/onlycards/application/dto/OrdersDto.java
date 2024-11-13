package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OrdersDto {
    @NotNull
    private UUID id;

    @NotNull
    private Status status;
    @NotNull
    private LocalDate addDate;

    private LocalDateTime modifyDate;
    private String userLastEdit;

    @NotNull
    private String vendorId;
    @NotNull
    private AccountDto user;
    @NotNull
    private List<TransactionsDto> transactions;
}
