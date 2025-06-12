package auth.service;

import static auth.service.app.util.ConstantUtils.ENV_DB_PASSWORD;
import static auth.service.app.util.ConstantUtils.ENV_DB_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import helper.TestData;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class AppTest extends BaseTest {

  @Test
  void shouldFailToStartApplicationOnMissingEnvProperty() {
    try (MockedStatic<CommonUtilities> mockedStatic = mockStatic(CommonUtilities.class)) {
      Map<String, String> requiredEnvProperties = TestData.getSystemEnvPropertyTestData();
      requiredEnvProperties.remove(ENV_DB_USERNAME);
      requiredEnvProperties.remove(ENV_DB_PASSWORD);

      mockedStatic
          .when(() -> CommonUtilities.getSystemEnvProperties(any()))
          .thenReturn(requiredEnvProperties);

      IllegalStateException exception =
          assertThrows(IllegalStateException.class, () -> App.main(new String[] {}));

      assertEquals(
          "One or more environment configurations could not be accessed...",
          exception.getMessage());
    }
  }
}
