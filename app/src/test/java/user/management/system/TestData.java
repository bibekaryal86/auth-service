package user.management.system;

import static user.management.system.app.util.ConstantUtils.ENV_KEY_NAMES;
import static user.management.system.app.util.ConstantUtils.ENV_SERVER_PORT;

import java.util.Map;
import java.util.stream.Collectors;

public class TestData {

  public static Map<String, String> getSystemEnvPropertyTestData() {
    return ENV_KEY_NAMES.stream()
        .collect(Collectors.toMap(someKeyName -> someKeyName, someKeyName -> someKeyName));
  }

  public static void setSystemEnvPropertyTestData() {
    ENV_KEY_NAMES.forEach(
        env -> {
          if (!ENV_SERVER_PORT.equals(env)) {
            System.setProperty(env, env);
          }
        });
  }
}
