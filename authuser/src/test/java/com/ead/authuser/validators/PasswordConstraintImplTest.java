package com.ead.authuser.validators;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordConstraintImpl Tests")
class PasswordConstraintImplTest {

    private PasswordConstraintImpl validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new PasswordConstraintImpl();
    }

    // ============ VALID PASSWORDS ============

    @ParameterizedTest
    @ValueSource(strings = {
            "MyP@ssw0rd",      // Standard valid
            "Abc@1234",        // All requirements met
            "Test!1Pass",      // Different special char
            "Valid@123Pass",   // Longer password
            "X@1abcde",        // Exactly 8 chars
            "MyP@ss0wd",       // With underscore in mind
            "Secure!P@ss1"     // Multiple special chars
    })
    @DisplayName("isValid should return true for valid passwords")
    void isValid_ValidPasswords_ReturnsTrue(String validPassword) {
        boolean result = validator.isValid(validPassword, context);

        assertThat(result).isTrue();
    }

    // ============ INVALID PASSWORDS - Missing Requirements ============

    @Test
    @DisplayName("isValid should return false for password without uppercase letter")
    void isValid_NoUppercase_ReturnsFalse() {
        String password = "myp@ssw0rd";  // No uppercase

        boolean result = validator.isValid(password, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid should return false for password without lowercase letter")
    void isValid_NoLowercase_ReturnsFalse() {
        String password = "MYP@SSW0RD";  // No lowercase

        boolean result = validator.isValid(password, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid should return false for password without digit")
    void isValid_NoDigit_ReturnsFalse() {
        String password = "MyPassword@";  // No digit

        boolean result = validator.isValid(password, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid should return false for password without special character")
    void isValid_NoSpecialChar_ReturnsFalse() {
        String password = "MyPassword123";  // No special char

        boolean result = validator.isValid(password, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid should return false for password with only digits")
    void isValid_OnlyDigits_ReturnsFalse() {
        String password = "1234567";  // Only digits

        boolean result = validator.isValid(password, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid should return false for password with only letters")
    void isValid_OnlyLetters_ReturnsFalse() {
        String password = "MyPassword";  // Only letters

        boolean result = validator.isValid(password, context);

        assertThat(result).isFalse();
    }

    // ============ INVALID PASSWORDS - Length Issues ============

    @Test
    @DisplayName("isValid should return false for password shorter than 6 characters")
    void isValid_TooShort_ReturnsFalse() {
        String password = "My@1";  // 4 chars, min is 6

        boolean result = validator.isValid(password, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid should return false for password longer than 20 characters")
    void isValid_TooLong_ReturnsFalse() {
        String password = "MyP@ssword123456789XY";  // 21 chars, max is 20

        boolean result = validator.isValid(password, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid should return true for password with exactly 6 characters")
    void isValid_ExactlyMinLength_ReturnsTrue() {
        String password = "MyP@s1";  // Exactly 6 chars

        boolean result = validator.isValid(password, context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isValid should return true for password with exactly 20 characters")
    void isValid_ExactlyMaxLength_ReturnsTrue() {
        String password = "MyPassword@1234567ab";  // Exactly 20 chars

        boolean result = validator.isValid(password, context);

        assertThat(result).isTrue();
    }

    // ============ INVALID PASSWORDS - Spaces ============

    @Test
    @DisplayName("isValid should return false for password with spaces")
    void isValid_WithSpaces_ReturnsFalse() {
        String password = "MyP@ss word";  // Contains space

        boolean result = validator.isValid(password, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid should return false for password with leading space")
    void isValid_WithLeadingSpace_ReturnsFalse() {
        String password = " MyP@ssw0rd";  // Starts with space

        boolean result = validator.isValid(password, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid should return false for password with trailing space")
    void isValid_WithTrailingSpace_ReturnsFalse() {
        String password = "MyP@ssw0rd ";  // Ends with space

        boolean result = validator.isValid(password, context);

        assertThat(result).isFalse();
    }

    // ============ NULL AND EMPTY ============

    @Test
    @DisplayName("isValid should return false for null password")
    void isValid_NullPassword_ReturnsFalse() {
        boolean result = validator.isValid(null, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid should return false for empty password")
    void isValid_EmptyPassword_ReturnsFalse() {
        boolean result = validator.isValid("", context);

        assertThat(result).isFalse();
    }

    // ============ EDGE CASES WITH SPECIAL CHARACTERS ============

    @ParameterizedTest
    @ValueSource(strings = {
            "Test!@#$%^&*()-1aB",   // <= 20 chars,  // Many special chars
            "A1b!09",                          // Minimum requirement
            "Pass@123Word",                  // Standard format
            "M0d3rn!P@ss",                   // Multiple special chars
            "Sec@re1ty"                      // Simple valid
    })
    @DisplayName("isValid should handle various special characters correctly")
    void isValid_VariousSpecialChars_ReturnsTrue(String password) {
        boolean result = validator.isValid(password, context);

        assertThat(result).isTrue();
    }

    // ============ REAL WORLD SCENARIOS ============

    @Test
    @DisplayName("isValid should accept strong password from real scenario")
    void isValid_StrongPassword_ReturnsTrue() {
        String password = "MySecur3P@ss!";

        boolean result = validator.isValid(password, context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isValid should reject weak password from real scenario")
    void isValid_WeakPassword_ReturnsFalse() {
        String password = "password123";  // Missing uppercase and special char

        boolean result = validator.isValid(password, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid should reject common weak password patterns")
    void isValid_CommonWeakPatterns_ReturnsFalse() {
        String[] weakPasswords = {
                "Admin123",        // No special char
                "test@123",        // No uppercase
                "TEST@PASSWORD",   // No lowercase or digit
                "12345678",        // Only digits
                "abcdefgh"         // Only letters
        };

        for (String weak : weakPasswords) {
            boolean result = validator.isValid(weak, context);
            assertThat(result).isFalse();
        }
    }

    @Test
    @DisplayName("isValid should accept all common strong patterns")
    void isValid_CommonStrongPatterns_ReturnsTrue() {
        String[] strongPasswords = {
                "MyP@ssw0rd",
                "Secure!123Pass",
                "Test#2024Valid",
                "P@ssw0rd!Test",
                "Str0ng!Pass@2024"
        };

        for (String strong : strongPasswords) {
            boolean result = validator.isValid(strong, context);
            assertThat(result).isTrue();
        }
    }
}

