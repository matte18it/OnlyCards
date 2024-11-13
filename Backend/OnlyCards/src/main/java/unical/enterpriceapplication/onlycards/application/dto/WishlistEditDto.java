package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WishlistEditDto {
    @Size(min = 3, max = 30)
    private String name;
    private Boolean isPublic= false;

}
