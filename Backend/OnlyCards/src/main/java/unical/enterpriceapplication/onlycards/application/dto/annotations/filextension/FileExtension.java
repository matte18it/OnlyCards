package unical.enterpriceapplication.onlycards.application.dto.annotations.filextension;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = FileExtensionValidator.class)
public @interface FileExtension {
    String message() default "Invalid or not allowed file extension";
        Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] extensions() ;
}
