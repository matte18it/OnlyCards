package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AccountDto {
    @NotNull
    private UUID id;
    @NotBlank
    private String username;
    @NotBlank
    private String email;
}
