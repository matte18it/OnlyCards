package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Status;

@Data
public class OrderEditDto {
    @NotNull
    private Status status;
}
