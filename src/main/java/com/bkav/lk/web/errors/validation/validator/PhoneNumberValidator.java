package com.bkav.lk.web.errors.validation.validator;

import com.bkav.lk.web.errors.validation.PhoneNumberConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumberConstraint, String> {

    private static final String PHONE_NUMBER_REGEX_PATTERN = "(03|05|07|08|09)[0|1|2|3|4|5|6|7|8|9]([0-9]{7})";



    @Override
    public void initialize(PhoneNumberConstraint constraintAnnotation) {}

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return s != null && s.matches(PHONE_NUMBER_REGEX_PATTERN);
    }
}
