package com.oauth2cloud.server.hibernate.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NoSpacesValidator implements ConstraintValidator<NoSpaces, String> {
    @Override
    public void initialize(NoSpaces noSpaces) {
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return noSpaces(s);
    }

    public static boolean noSpaces(String s) {
        return s == null || !s.contains(" ");
    }
}
