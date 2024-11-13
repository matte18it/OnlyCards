package unical.enterpriceapplication.onlycards.application.dto.annotations.trustedurl;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = TrustedUrlValidator.class)
public @interface TrustedUrl {
    String message() default "Url not trusted";
        Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}