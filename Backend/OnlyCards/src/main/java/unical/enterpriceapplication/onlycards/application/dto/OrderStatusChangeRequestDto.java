package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderStatusChangeRequestDto {
    @NotNull
    private String status;
    @NotNull
    private UUID userId;
}
