package auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import auth.service.app.util.ConstantUtils;
import helper.TestData;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@DisplayName("Main App Unit Tests")
public class AppTest {

  @Test
  @DisplayName("Should fail if env properties are missing")
  void shouldFailIfEnvPropertiesAreMissing() {
    try (MockedStatic<CommonUtilities> mockedStatic = mockStatic(CommonUtilities.class)) {
      Map<String, String> requiredEnvProperties = TestData.getSystemEnvPropertyTestData();
      requiredEnvProperties.remove(ConstantUtils.ENV_DB_USERNAME_SANDBOX);
      requiredEnvProperties.remove(ConstantUtils.ENV_DB_USERNAME_PROD);
      requiredEnvProperties.remove(ConstantUtils.ENV_DB_PASSWORD_SANDBOX);
      requiredEnvProperties.remove(ConstantUtils.ENV_DB_PASSWORD_PROD);

      mockedStatic
          .when(() -> CommonUtilities.getSystemEnvProperties(any()))
          .thenReturn(requiredEnvProperties);
      mockedStatic
          .when(() -> CommonUtilities.getSystemEnvProperty(any()))
          .thenReturn("springboottest");

      IllegalStateException exception =
          assertThrows(IllegalStateException.class, App::validateInitArgs);

      assertEquals(
          "One or more environment configurations could not be accessed...",
          exception.getMessage());
    }
  }
}
