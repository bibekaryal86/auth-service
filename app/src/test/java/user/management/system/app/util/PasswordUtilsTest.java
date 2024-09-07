package user.management.system.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import user.management.system.BaseTest;

public class PasswordUtilsTest extends BaseTest {

  @MockBean private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired private PasswordUtils passwordUtils;

  @Test
  public void testHashPassword() {
    // Arrange
    String plainPassword = "myPassword";
    String hashedPassword = "$2a$10$hashValue";

    when(bCryptPasswordEncoder.encode(plainPassword)).thenReturn(hashedPassword);

    // Act
    String result = passwordUtils.hashPassword(plainPassword);

    // Assert
    assertNotNull(result);
    assertEquals(hashedPassword, result);
    verify(bCryptPasswordEncoder, times(1)).encode(plainPassword);
  }

  @Test
  public void testVerifyPassword_Success() {
    // Arrange
    String plainPassword = "myPassword";
    String hashedPassword = "$2a$10$hashValue";

    when(bCryptPasswordEncoder.matches(plainPassword, hashedPassword)).thenReturn(true);

    // Act
    boolean result = passwordUtils.verifyPassword(plainPassword, hashedPassword);

    // Assert
    assertTrue(result);
    verify(bCryptPasswordEncoder, times(1)).matches(plainPassword, hashedPassword);
  }

  @Test
  public void testVerifyPassword_Failure() {
    // Arrange
    String plainPassword = "myPassword";
    String hashedPassword = "$2a$10$hashValue";

    when(bCryptPasswordEncoder.matches(plainPassword, hashedPassword)).thenReturn(false);

    // Act
    boolean result = passwordUtils.verifyPassword(plainPassword, hashedPassword);

    // Assert
    assertFalse(result);
    verify(bCryptPasswordEncoder, times(1)).matches(plainPassword, hashedPassword);
  }
}
