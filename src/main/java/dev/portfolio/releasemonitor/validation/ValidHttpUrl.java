package dev.portfolio.releasemonitor.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HttpUrlValidator.class)
public @interface ValidHttpUrl {

    String message() default "must be a valid HTTP or HTTPS URL";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
