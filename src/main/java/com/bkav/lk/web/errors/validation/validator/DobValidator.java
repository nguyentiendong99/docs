package com.bkav.lk.web.errors.validation.validator;

import com.bkav.lk.web.errors.validation.DobConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.util.Objects;

public class DobValidator implements ConstraintValidator<DobConstraint, Instant> {

    @Override
    public void initialize(DobConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(Instant s, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.nonNull(s))
            return !Instant.now().isBefore(s);
        else
            return true;
    }
}
