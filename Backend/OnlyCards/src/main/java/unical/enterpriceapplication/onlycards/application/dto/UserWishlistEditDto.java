package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserWishlistEditDto {
@NotBlank
    @Size(min = 3, max = 50)
    private String username;
}
