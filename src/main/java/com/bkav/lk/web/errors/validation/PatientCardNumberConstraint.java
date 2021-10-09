package com.bkav.lk.web.errors.validation;

import com.bkav.lk.web.errors.validation.validator.PatientCardNumber;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PatientCardNumber.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PatientCardNumberConstraint {
    String message() default "Invalid card number format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
