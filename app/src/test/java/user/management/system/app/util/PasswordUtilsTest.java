package user.management.system.app.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import user.management.system.BaseTest;

public class PasswordUtilsTest extends BaseTest {

  @Autowired private PasswordUtils passwordUtils;

  @Test
  public void testHashPassword() {
    // Arrange
    String plainPassword = "myPassword";
    // Act
    String result = passwordUtils.hashPassword(plainPassword);

    // Assert
    assertNotNull(result);
  }

  @Test
  public void testVerifyPassword_Success() {
    // Arrange
    String plainPassword = "myPassword";
    String hashedPassword = passwordUtils.hashPassword(plainPassword);

    // Act
    boolean result = passwordUtils.verifyPassword(plainPassword, hashedPassword);

    // Assert
    assertTrue(result);
  }

  @Test
  public void testVerifyPassword_Failure() {
    // Arrange
    String plainPassword = "myPassword";

    // Act
    boolean result = passwordUtils.verifyPassword(plainPassword, "hashedPassword");

    // Assert
    assertFalse(result);
  }
}
