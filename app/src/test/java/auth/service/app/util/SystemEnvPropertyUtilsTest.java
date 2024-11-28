package auth.service.app.util;

import static auth.service.app.util.ConstantUtils.ENV_DB_PASSWORD;
import static auth.service.app.util.ConstantUtils.ENV_DB_USERNAME;
import static auth.service.app.util.ConstantUtils.ENV_KEY_NAMES;
import static auth.service.app.util.ConstantUtils.ENV_SERVER_PORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import auth.service.BaseTest;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class SystemEnvPropertyUtilsTest extends BaseTest {

  @Test
  public void testGetSystemEnvPropertyWithDefault() {
    String result = SystemEnvPropertyUtils.getSystemEnvProperty(ENV_DB_USERNAME, "defaultValue");
    assertEquals(ENV_DB_USERNAME, result);

    result = SystemEnvPropertyUtils.getSystemEnvProperty(ENV_SERVER_PORT, "defaultValue");
    assertEquals("defaultValue", result);
  }

  @Test
  public void testGetSystemEnvProperty() {
    String result = SystemEnvPropertyUtils.getSystemEnvProperty(ENV_DB_PASSWORD);
    assertEquals(ENV_DB_PASSWORD, result);

    result = SystemEnvPropertyUtils.getSystemEnvProperty(ENV_SERVER_PORT);
    assertNull(result);
  }

  @Test
  public void testGetAllSystemEnvProperties() {
    Map<String, String> properties = SystemEnvPropertyUtils.getAllSystemEnvProperties();
    assertEquals(ENV_KEY_NAMES.size() - 1, properties.size()); // -1 for PORT
    assertEquals(ENV_DB_USERNAME, properties.get(ENV_DB_USERNAME));
    assertEquals(ENV_DB_PASSWORD, properties.get(ENV_DB_PASSWORD));
  }
}
