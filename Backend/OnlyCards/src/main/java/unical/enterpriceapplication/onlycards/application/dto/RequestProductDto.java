package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class RequestProductDto {
    @NotNull
    private MultipartFile image;
    @NotNull
    private String game;
    @NotNull
    private String name;
    @NotNull
    private String message;
    @NotNull
    private UUID id;
}
