package unical.enterpriceapplication.onlycards.application.dto.annotations.photorphotourl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Payload;
import jakarta.validation.Constraint;

@Constraint(validatedBy = PhotoOrPhotoUrlValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PhotoOrPhotoUrl {
    String message() default "Either photo or photoUrl must be provided, but not both.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
