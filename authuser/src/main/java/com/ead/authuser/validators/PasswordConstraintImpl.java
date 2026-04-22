package com.ead.authuser.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordConstraintImpl implements ConstraintValidator<PasswordConstraint, String> {

    private static final String PASSWORD_PATTERN = """
    ^(?=.*[0-9])
     (?=.*[a-z])
     (?=.*[A-Z])
     (?=.*[^a-zA-Z0-9])
     (?=\\S+$)
     .{6,20}$
    """.replaceAll("\\s+", "");

    private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);

    @Override
    public void initialize(PasswordConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty() || password.contains(" ")) {
            return false;
        }
        return PATTERN.matcher(password).matches();
    }
}