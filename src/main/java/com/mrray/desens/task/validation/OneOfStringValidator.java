package com.mrray.desens.task.validation;

import com.mrray.desens.task.validation.annotaion.OneOfString;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

public class OneOfStringValidator implements ConstraintValidator<OneOfString, String> {

    private String[] value;

    @Override
    public void initialize(OneOfString oneOf) {
        this.value = oneOf.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (!StringUtils.hasText(value)) {
            return false;
        }

        return Stream.of(this.value).anyMatch(v -> v.equalsIgnoreCase(value));
    }
}
