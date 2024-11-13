package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserLoginDto {
    @NotNull
    private UUID id;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private List<RoleDto> roles;
    @NotNull
    private Boolean blocked;
}
