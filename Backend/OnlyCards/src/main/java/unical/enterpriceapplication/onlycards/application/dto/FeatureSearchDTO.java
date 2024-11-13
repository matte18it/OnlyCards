package unical.enterpriceapplication.onlycards.application.dto;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FeatureSearchDTO {
    @NotBlank
    @Size(max = 50)
    private String name;
    @NotEmpty
    @Valid
    private Set<@NotBlank @Size(max = 100) String> value;
}
