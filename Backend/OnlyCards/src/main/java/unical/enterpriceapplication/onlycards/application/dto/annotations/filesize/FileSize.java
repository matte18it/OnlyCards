package unical.enterpriceapplication.onlycards.application.dto.annotations.filesize;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = FileSizeValidator.class)
public @interface FileSize {
    String message() default "File size exceeds the maximum limit ";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    long max();

}
