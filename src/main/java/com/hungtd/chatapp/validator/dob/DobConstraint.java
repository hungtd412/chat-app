package com.hungtd.chatapp.validator.dob;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD}) //only apply to fields
@Retention(RetentionPolicy.RUNTIME) //apply for runtime
@Constraint(
        validatedBy = {DobValidator.class}
) //class responsible for handling this validation
public @interface DobConstraint {
    String message();

    int min() default 18;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


}
