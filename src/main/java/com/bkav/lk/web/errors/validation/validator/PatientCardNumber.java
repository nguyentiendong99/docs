package com.bkav.lk.web.errors.validation.validator;

import com.bkav.lk.web.errors.validation.PatientCardNumberConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PatientCardNumber implements ConstraintValidator<PatientCardNumberConstraint, String> {
    private static final String REGEX_PATTERN = "^[0-9]+$";

    @Override
    public void initialize(PatientCardNumberConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return s != null && s.matches(REGEX_PATTERN);
    }
}
