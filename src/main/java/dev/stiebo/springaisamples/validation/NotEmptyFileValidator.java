package dev.stiebo.springaisamples.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class NotEmptyFileValidator implements
        ConstraintValidator<NotEmptyFile, MultipartFile> {
    @Override
    public void initialize(NotEmptyFile constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        return (file != null && !file.isEmpty());
    }
}
