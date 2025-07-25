package com.aiframework.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Pattern(regexp = "^[A-Z]{1,5}$", message = "Stock symbol must be 1-5 uppercase letters")
public @interface ValidStockSymbol {
    String message() default "Invalid stock symbol format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
