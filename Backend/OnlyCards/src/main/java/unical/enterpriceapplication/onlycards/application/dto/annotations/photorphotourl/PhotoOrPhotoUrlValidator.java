package unical.enterpriceapplication.onlycards.application.dto.annotations.photorphotourl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import unical.enterpriceapplication.onlycards.application.dto.ProductTypeRegistrationDto;

public class PhotoOrPhotoUrlValidator implements ConstraintValidator<PhotoOrPhotoUrl, ProductTypeRegistrationDto> {
        @Override
    public boolean isValid(ProductTypeRegistrationDto dto, ConstraintValidatorContext context) {
        boolean isPhotoPresent = dto.getPhoto() != null && !dto.getPhoto().isEmpty();
        boolean isPhotoUrlPresent = dto.getPhotoUrl() != null && !dto.getPhotoUrl().isEmpty();

          // True se uno è nullo e l'altro no, oppure se entrambi sono nulli o vuoti
        return !(isPhotoPresent && isPhotoUrlPresent);
    }
}
