package com.ead.authuser.validators;

import com.ead.authuser.exceptions.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("PasswordValidator Tests")
class PasswordValidatorTest {

    @Test
    @DisplayName("validateNotBlank should pass when both passwords are valid")
    void validateNotBlank_Valid() {
        assertThatCode(() -> PasswordValidator.validateNotBlank("old", "new"))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateNotBlank should fail when oldPassword is blank")
    void validateNotBlank_OldPasswordInvalid() {
        assertThatThrownBy(() -> PasswordValidator.validateNotBlank("", "new"))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Old password is required.");
    }

    @Test
    @DisplayName("validateNotBlank should fail when newPassword is null or blank")
    void validateNotBlank_NewPasswordInvalid() {
        assertThatThrownBy(() -> PasswordValidator.validateNotBlank("old", ""))
            .isInstanceOf(BusinessException.class)
            .hasMessage("New password is required.");
    }

    @Test
    @DisplayName("validateStrength should pass for valid password")
    void validateStrength_Valid() {
        assertThatCode(() -> PasswordValidator.validateStrength("ValidPass123"))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateStrength should fail for password with spaces")
    void validateStrength_WithSpaces() {
        assertThatThrownBy(() -> PasswordValidator.validateStrength("Pass word"))
            .isInstanceOf(BusinessException.class)
            .hasMessage("New password cannot contain spaces.");
    }

    @Test
    @DisplayName("validateStrength should fail for password less than 6 characters")
    void validateStrength_TooShort() {
        assertThatThrownBy(() -> PasswordValidator.validateStrength("Pass1"))
            .isInstanceOf(BusinessException.class)
            .hasMessage("New password must be at least 6 characters.");
    }

    @Test
    @DisplayName("validateDifferent should pass when passwords are different")
    void validateDifferent_Different() {
        assertThatCode(() -> PasswordValidator.validateDifferent("old", "new"))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateDifferent should fail when passwords are identical")
    void validateDifferent_Identical() {
        assertThatThrownBy(() -> PasswordValidator.validateDifferent("same", "same"))
            .isInstanceOf(BusinessException.class)
            .hasMessage("New password must be different from old password.");
    }

    @Test
    @DisplayName("validateStrength should pass for exactly 6 character password")
    void validateStrength_ExactlyMinimum() {
        assertThatCode(() -> PasswordValidator.validateStrength("Passw0"))
            .doesNotThrowAnyException();
    }
}

