package unical.enterpriceapplication.onlycards.application.dto.annotations.filextension;
import java.util.Arrays;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FileExtensionValidator implements ConstraintValidator<FileExtension, MultipartFile> {
    private String[] extensions;

    @Override
    public void initialize(FileExtension constraintAnnotation) {
        this.extensions = constraintAnnotation.extensions();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // Controllo se il file è null (opzionale: gestire la validazione null separatamente)
        if (file == null || file.isEmpty()) {
            return true; // Considera null o vuoto come valido, puoi gestire diversamente se necessario
        }

        // Verifica l'estensione del file
        String fileExtension = getFileExtension(file.getOriginalFilename());
        boolean isValidExtension = Arrays.stream(extensions)
                .anyMatch(ext -> ext.equalsIgnoreCase(fileExtension));

        if (!isValidExtension) {
            String extensionList = String.join(", ", extensions);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Invalid file extension. Permitted extensions are: " + extensionList
            ).addConstraintViolation();
            return false;
        }
        return true;
    }
    private String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        return (lastIndexOfDot == -1) ? "" : fileName.substring(lastIndexOfDot + 1);
    }
}
