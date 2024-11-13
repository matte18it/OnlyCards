package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdvancedSearchDto {
    @NotBlank
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String image;
    @NotBlank
    private String setName;
    @NotBlank
    private String collectorNumber;
    @NotBlank
    private String rarity;
    @NotBlank
    private String type;
}
