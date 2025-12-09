package com.ead.authuser.validators;

import com.ead.authuser.exceptions.ConflictException;

public class PasswordValidator {

    public static void validateNotBlank(String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new ConflictException("Old password is required.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new ConflictException("New password is required.");
        }
    }

    public static void validateStrength(String newPassword) {
        if (newPassword.contains(" ")) {
            throw new ConflictException("New password cannot contain spaces.");
        }
        if (newPassword.length() < 6) {
            throw new ConflictException("New password must be at least 6 characters.");
        }
    }

    public static void validateDifferent(String oldPassword, String newPassword) {
        if (oldPassword.equals(newPassword)) {
            throw new ConflictException("New password must be different from old password.");
        }
    }
}
