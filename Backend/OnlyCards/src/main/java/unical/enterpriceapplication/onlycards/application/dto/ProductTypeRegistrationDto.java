package unical.enterpriceapplication.onlycards.application.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import unical.enterpriceapplication.onlycards.application.dto.annotations.filesize.FileSize;
import unical.enterpriceapplication.onlycards.application.dto.annotations.filextension.FileExtension;
import unical.enterpriceapplication.onlycards.application.dto.annotations.photorphotourl.PhotoOrPhotoUrl;
import unical.enterpriceapplication.onlycards.application.dto.annotations.trustedurl.TrustedUrl;



@Data
@PhotoOrPhotoUrl
public class ProductTypeRegistrationDto {
    @Size(max = 50, min = 3)
    @NotBlank
    private String name;
    @Size(max = 50, min = 2)
    @NotBlank
    private String language;
    @Size(max = 50, min = 3)
    @NotBlank
    private String game;
    @NotBlank
    @Size(max = 50, min = 3)
    private String type;

    @FileExtension(extensions = {"jpg", "jpeg", "png"})
    @FileSize(max = 20 * 1024 * 1024) // 20MB
    private MultipartFile photo;
    @TrustedUrl
    private String photoUrl;
    @Valid
    private List<FeatureDTO> features = new ArrayList<>();
}
