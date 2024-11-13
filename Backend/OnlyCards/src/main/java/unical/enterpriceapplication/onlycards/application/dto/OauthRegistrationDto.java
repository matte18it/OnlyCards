package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;



@Data
public class OauthRegistrationDto {
    @NotBlank
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Only letters, numbers and underscore are allowed")
    private String username;
}
