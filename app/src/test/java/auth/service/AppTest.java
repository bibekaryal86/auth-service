package auth.service;

import static auth.service.app.util.ConstantUtils.ENV_DB_PASSWORD;
import static auth.service.app.util.ConstantUtils.ENV_DB_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import helper.TestData;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class AppTest extends BaseTest {

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
