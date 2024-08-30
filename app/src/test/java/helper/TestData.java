package helper;

import static user.management.system.app.util.ConstantUtils.ENV_KEY_NAMES;
import static user.management.system.app.util.ConstantUtils.ENV_SERVER_PORT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import user.management.system.app.model.client.EnvDetails;

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

  public static List<EnvDetails> getEnvDetailsResponse() {
    String fixtureAsString =
        FixtureReader.readFixture("authenv-service_getPropertiesResponse.json");
    try {
      return ObjectMapperProvider.objectMapper()
          .readValue(fixtureAsString, new TypeReference<>() {});
    } catch (JsonProcessingException ex) {
      return Collections.emptyList();
    }
  }
}
