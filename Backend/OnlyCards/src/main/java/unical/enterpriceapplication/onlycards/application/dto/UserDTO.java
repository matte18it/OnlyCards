package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class UserDTO {
    @NotNull
    private UUID id;
    @NotEmpty
    private Set<RoleDto> roles = new HashSet<>();
    private Set<AddressDto> addresses = new HashSet<>();
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String username;
    @NotBlank
    private String cellphoneNumber;
    private boolean oauthUser;

    private boolean blocked;


}
