package user.management.system;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import user.management.system.app.util.SystemEnvPropertyUtils;

public class AppTest extends BaseTest {

  @Test
  void shouldStartApplicationWithDefaultPort() {
    try (MockedStatic<SystemEnvPropertyUtils> utilities =
        mockStatic(SystemEnvPropertyUtils.class)) {
      utilities
          .when(() -> SystemEnvPropertyUtils.getSystemEnvProperty("SERVER_PORT", "8080"))
          .thenReturn("8080");
      App.main(new String[] {});
    }
  }
}
