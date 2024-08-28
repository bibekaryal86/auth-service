package user.management.system;

import static user.management.system.app.util.ConstantUtils.ENV_KEY_NAMES;
import static user.management.system.app.util.ConstantUtils.ENV_SERVER_PORT;

public class TestData {

  public static void setSystemEnvPropertyTestData() {
    ENV_KEY_NAMES.forEach(env -> {
      if (!ENV_SERVER_PORT.equals(env)) {
        System.setProperty(env, env);
      }
    });
  }
}
