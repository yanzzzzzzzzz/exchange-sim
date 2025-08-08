package account.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation Logic Tests")
class ValidationTest {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{3,32}$");

    @Test
    @DisplayName("Email 驗證測試")
    void testEmailValidation() {
        // 有效的 email
        assertTrue(isValidEmail("test@example.com"));
        assertTrue(isValidEmail("user.name@domain.co.uk"));
        assertTrue(isValidEmail("user+tag@example.org"));
        assertTrue(isValidEmail("123@example.com"));

        // 無效的 email
        assertFalse(isValidEmail("invalid-email"));
        assertFalse(isValidEmail("@example.com"));
        assertFalse(isValidEmail("test@"));
        assertFalse(isValidEmail("test.example.com"));
        assertFalse(isValidEmail("test@.com"));
        assertFalse(isValidEmail(""));
        assertFalse(isValidEmail(null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "password123", "1234567", "abcdefg", "P@ssw0rd!", "very_long_password_123" })
    @DisplayName("有效密碼長度測試")
    void testValidPasswordLength(String password) {
        assertTrue(isValidPassword(password), "Password should be valid: " + password);
    }

    @ParameterizedTest
    @ValueSource(strings = { "123456", "abc", "", "12345" })
    @DisplayName("無效密碼長度測試")
    void testInvalidPasswordLength(String password) {
        assertFalse(isValidPassword(password), "Password should be invalid: " + password);
    }

    @Test
    @DisplayName("Username 驗證測試")
    void testUsernameValidation() {
        // 有效的 username
        assertTrue(isValidUsername("abc"));
        assertTrue(isValidUsername("user123"));
        assertTrue(isValidUsername("test_user"));
        assertTrue(isValidUsername("user-name"));
        assertTrue(isValidUsername("a".repeat(32))); // 32 字元

        // 無效的 username
        assertFalse(isValidUsername("ab")); // 太短
        assertFalse(isValidUsername("a".repeat(33))); // 太長
        assertFalse(isValidUsername("user@name")); // 包含無效字元
        assertFalse(isValidUsername("user name")); // 包含空格
        assertFalse(isValidUsername("user.name")); // 包含點
        assertFalse(isValidUsername(""));
        assertFalse(isValidUsername(null));
    }

    @Test
    @DisplayName("邊界值測試")
    void testBoundaryValues() {
        // 密碼邊界值
        assertTrue(isValidPassword("1234567")); // 剛好 7 字元
        assertFalse(isValidPassword("123456")); // 6 字元

        // Username 邊界值
        assertTrue(isValidUsername("abc")); // 剛好 3 字元
        assertTrue(isValidUsername("a".repeat(32))); // 剛好 32 字元
        assertFalse(isValidUsername("ab")); // 2 字元
        assertFalse(isValidUsername("a".repeat(33))); // 33 字元

    }

    // Helper methods (實際實作中這些會在 service 或 validator 類別中)
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 7;
    }

    private boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

}