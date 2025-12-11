package unit.auth.service.app.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import auth.service.app.util.PasswordUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Tag("unit")
@DisplayName("PasswordUtils Unit Tests")
class PasswordUtilsTest {

  private final BCryptPasswordEncoder encoder = Mockito.mock(BCryptPasswordEncoder.class);
  private final PasswordUtils passwordUtils = new PasswordUtils(encoder);

  @Nested
  @DisplayName("hashPassword() Tests")
  class HashPasswordTests {

    @Test
    @DisplayName("Should return hashed password when plain password is valid")
    void shouldReturnHashedPassword() {
      String plain = "myPassword123";
      String hashed = "$2a$10$abcdefgHashedPasswordHere";

      when(encoder.encode(plain)).thenReturn(hashed);
      String result = passwordUtils.hashPassword(plain);

      assertEquals(hashed, result);
      verify(encoder).encode(plain);
    }

    @Test
    @DisplayName("Should hash empty string without throwing error")
    void shouldHashEmptyString() {
      String plain = "";
      String hashed = "$2a$10$emptyStringHash";

      when(encoder.encode(plain)).thenReturn(hashed);
      String result = passwordUtils.hashPassword(plain);

      assertEquals(hashed, result);
      verify(encoder).encode(plain);
    }

    @Test
    @DisplayName("Should throw exception when encoder fails")
    void shouldThrowExceptionWhenEncoderFails() {
      String plain = "password";
      when(encoder.encode(plain)).thenThrow(new IllegalStateException("Encoder failure"));
      assertThrows(IllegalStateException.class, () -> passwordUtils.hashPassword(plain));
    }
  }

  @Nested
  @DisplayName("verifyPassword() Tests")
  class VerifyPasswordTests {

    @Test
    @DisplayName("Should return true when plain password matches hashed password")
    void shouldReturnTrueForValidMatch() {
      String plain = "myPassword";
      String hashed = "$2a$10$abcd1234hashValue";

      when(encoder.matches(plain, hashed)).thenReturn(true);
      boolean result = passwordUtils.verifyPassword(plain, hashed);

      assertTrue(result);
      verify(encoder).matches(plain, hashed);
    }

    @Test
    @DisplayName("Should return false when plain password does NOT match hashed password")
    void shouldReturnFalseForInvalidMatch() {
      String plain = "wrongPassword";
      String hashed = "$2a$10$abcd1234hashValue";
      when(encoder.matches(plain, hashed)).thenReturn(false);

      boolean result = passwordUtils.verifyPassword(plain, hashed);

      assertFalse(result);
      verify(encoder).matches(plain, hashed);
    }

    @Test
    @DisplayName("Should support empty passwords (encoder decides the result)")
    void shouldHandleEmptyPasswords() {
      String plain = "";
      String hashed = "$2a$10$emptyHash";

      when(encoder.matches(plain, hashed)).thenReturn(false);
      boolean result = passwordUtils.verifyPassword(plain, hashed);

      assertFalse(result);
      verify(encoder).matches(plain, hashed);
    }

    @Test
    @DisplayName("Should throw exception when encoder fails")
    void shouldThrowExceptionWhenEncoderFails() {
      String plain = "test";
      String hashed = "hash123";

      when(encoder.matches(plain, hashed)).thenThrow(new RuntimeException("Failure"));
      assertThrows(RuntimeException.class, () -> passwordUtils.verifyPassword(plain, hashed));
    }
  }
}
