package dev.stiebo.openaiutils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotEmptyFileValidator.class)
@Target( { ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyFile {
    String message() default "No valid file found";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
