package user.management.system.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static user.management.system.app.util.ConstantUtils.ENV_DB_PASSWORD;
import static user.management.system.app.util.ConstantUtils.ENV_DB_USERNAME;
import static user.management.system.app.util.ConstantUtils.ENV_SERVER_PORT;

import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SystemEnvPropertyUtilsTest {

  @BeforeAll
  public static void setUp() {
    System.setProperty(ENV_DB_USERNAME, "test_username");
    System.setProperty(ENV_DB_PASSWORD, "test_password");
    SystemEnvPropertyUtils.getAllSystemEnvProperties();
  }

  @Test
  public void testGetSystemEnvPropertyWithDefault() {
    String result = SystemEnvPropertyUtils.getSystemEnvProperty(ENV_DB_USERNAME, "defaultValue");
    assertEquals("test_username", result);

    result = SystemEnvPropertyUtils.getSystemEnvProperty(ENV_SERVER_PORT, "defaultValue");
    assertEquals("defaultValue", result);
  }

  @Test
  public void testGetSystemEnvProperty() {
    String result = SystemEnvPropertyUtils.getSystemEnvProperty(ENV_DB_USERNAME);
    assertEquals("test_username", result);

    result = SystemEnvPropertyUtils.getSystemEnvProperty(ENV_DB_PASSWORD);
    assertEquals("test_password", result);

    result = SystemEnvPropertyUtils.getSystemEnvProperty(ENV_SERVER_PORT);
    assertNull(result);
  }

  @Test
  public void testGetAllSystemEnvProperties() {
    Map<String, String> properties = SystemEnvPropertyUtils.getAllSystemEnvProperties();
    assertEquals(2, properties.size());
    assertEquals("test_username", properties.get(ENV_DB_USERNAME));
    assertEquals("test_password", properties.get(ENV_DB_PASSWORD));
  }
}
