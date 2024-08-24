package user.management.system.app.util;

import static user.management.system.app.util.ConstantUtils.ENV_KEY_NAMES;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemEnvPropertyUtils {

  private static final Map<String, String> propertiesMap;

  static {
    Map<String, String> tempMap = new HashMap<>();

    final Properties systemProperties = System.getProperties();
    systemProperties.forEach(
        (key, value) -> {
          if (ENV_KEY_NAMES.contains(key.toString())) {
            tempMap.put((String) key, (String) value);
          }
        });

    final Map<String, String> envVariables = System.getenv();
    envVariables.forEach(
        (key, value) -> {
          if (ENV_KEY_NAMES.contains(key)) {
            tempMap.put(key, value);
          }
        });

    propertiesMap = Collections.unmodifiableMap(tempMap);
  }

  public static String getSystemEnvProperty(String key, String defaultValue) {
    return propertiesMap.getOrDefault(key, defaultValue);
  }

  public static String getSystemEnvProperty(String key) {
    return propertiesMap.get(key);
  }

  public static Map<String, String> getAllSystemEnvProperties() {
    return propertiesMap;
  }
}
