package unical.enterpriceapplication.onlycards.application.dto;

import org.springframework.web.multipart.MultipartFile;

import com.google.firebase.database.annotations.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import unical.enterpriceapplication.onlycards.application.dto.annotations.filesize.FileSize;
import unical.enterpriceapplication.onlycards.application.dto.annotations.filextension.FileExtension;

@Data
@NoArgsConstructor
public class ProductImageDto {
    @FileSize(max = 20971520)  // 20 MB
    @FileExtension(extensions = {"jpg", "jpeg", "png"})
    @NotNull
    private MultipartFile photo;
}
