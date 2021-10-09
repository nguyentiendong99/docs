package com.bkav.lk.web.errors.validation.validator;

import com.bkav.lk.web.errors.validation.EmailConstraint;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailValidator implements ConstraintValidator<EmailConstraint, String> {

    private static final String EMAIL_REGEX_PATTERN = "^[a-zA-Z][a-zA-Z0-9_\\.]{0,32}@[a-zA-Z0-9]+(\\.[a-zA-Z0-9]{2,4})+$";

    @Override
    public void initialize(EmailConstraint constraintAnnotation) {}

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (!StringUtils.isEmpty(s))
            return s.matches(EMAIL_REGEX_PATTERN);
        else
            return true;
    }
}
