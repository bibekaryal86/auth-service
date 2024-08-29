package user.management.system;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static user.management.system.app.util.ConstantUtils.ENV_DB_PASSWORD;
import static user.management.system.app.util.ConstantUtils.ENV_DB_USERNAME;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import user.management.system.app.util.SystemEnvPropertyUtils;

public class AppTest extends BaseTest {

  @Test
  void shouldStartApplication() {
    assertDoesNotThrow(() -> App.main(new String[] {}));
  }

  @Test
  void shouldFailToStartApplicationOnMissingEnvProperty() {
    try (MockedStatic<SystemEnvPropertyUtils> utilsMock =
        mockStatic(SystemEnvPropertyUtils.class)) {
      Map<String, String> requiredEnvProperties = TestData.getSystemEnvPropertyTestData();
      requiredEnvProperties.remove(ENV_DB_USERNAME);
      requiredEnvProperties.remove(ENV_DB_PASSWORD);

      utilsMock
          .when(SystemEnvPropertyUtils::getAllSystemEnvProperties)
          .thenReturn(requiredEnvProperties);

      IllegalStateException exception =
          assertThrows(IllegalStateException.class, () -> App.main(new String[] {}));

      assertEquals(
          "One or more environment configurations could not be accessed...",
          exception.getMessage());
    }
  }
}
