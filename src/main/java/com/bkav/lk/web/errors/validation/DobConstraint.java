package com.bkav.lk.web.errors.validation;

import com.bkav.lk.web.errors.validation.validator.DobValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DobValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DobConstraint {
    String message() default "Date of birth cannot be greater than the current date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
