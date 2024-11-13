package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Only letters, numbers and underscore are allowed")
    private String username;

    @NotBlank
    @Size(min = 8, max = 64)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,64}$", message = "Password must contain at least one lowercase letter, one uppercase letter, one number, and one special character")
    private String password;

    @Size(min = 6, max = 15)
    @Pattern(regexp = "^[0-9]+$", message = "Phone number can only contain digits")
    private String cellphoneNumber;
}