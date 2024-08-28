package user.management.system;

import static user.management.system.app.util.ConstantUtils.ENV_KEY_NAMES;

public class TestData {

  public static void setSystemEnvPropertyTestData() {
    ENV_KEY_NAMES.forEach(env -> System.setProperty(env, env));
  }
}
