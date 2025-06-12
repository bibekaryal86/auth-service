package auth.service.app.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import helper.FixtureReader;
import helper.ObjectMapperProvider;
import helper.TestData;
import io.github.bibekaryal86.shdsvc.AppEnvProperty;
import io.github.bibekaryal86.shdsvc.dtos.EnvDetailsResponse;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.env.Environment;

@Disabled
public class EnvServiceConnectorTest extends BaseTest {

  private EnvServiceConnector envServiceConnector;
  private Environment environment;
  private EnvDetailsResponse envDetailsResponse;
  private final String responseJsonFileName = "env-service_getPropertiesResponse.json";

  @BeforeEach
  void setUp() throws Exception {
    environment = mock(Environment.class);
    envServiceConnector = new EnvServiceConnector(environment);
    envDetailsResponse =
        ObjectMapperProvider.objectMapper()
            .readValue(FixtureReader.readFixture(responseJsonFileName), EnvDetailsResponse.class);
  }

  @AfterEach
  void tearDown() throws Exception {
    envServiceConnector.evictRedirectUrlCache();
    envServiceConnector.evictBaseUrlForLinkInEmailCache();
  }

  @Test
  void testGetRedirectUrls_Development() {
    when(environment.matchesProfiles("development")).thenReturn(true);

    try (MockedStatic<AppEnvProperty> mockedStatic = mockStatic(AppEnvProperty.class)) {
      mockedStatic
          .when(() -> AppEnvProperty.getEnvDetailsList("authsvc", true))
          .thenReturn(envDetailsResponse.getEnvDetails());

      Map<String, String> result = envServiceConnector.getRedirectUrls();

      assertNotNull(result);
      assertEquals(
          TestData.getEnvDetailsResponse().getEnvDetails().getFirst().getMapValue(), result);
    }
  }

  @Test
  void testGetRedirectUrls_Production() {
    when(environment.matchesProfiles("development")).thenReturn(false);

    try (MockedStatic<AppEnvProperty> mockedStatic = mockStatic(AppEnvProperty.class)) {
      mockedStatic
          .when(() -> AppEnvProperty.getEnvDetailsList("authsvc", true))
          .thenReturn(envDetailsResponse.getEnvDetails());

      Map<String, String> result = envServiceConnector.getRedirectUrls();

      assertNotNull(result);
      assertEquals(
          TestData.getEnvDetailsResponse().getEnvDetails().getFirst().getMapValue(), result);
    }
  }

  @Test
  void testGetBaseUrlForLinkInEmail_Development() {
    when(environment.matchesProfiles("development")).thenReturn(true);

    try (MockedStatic<AppEnvProperty> mockedStatic = mockStatic(AppEnvProperty.class)) {
      mockedStatic
          .when(() -> AppEnvProperty.getEnvDetailsList("authsvc", true))
          .thenReturn(envDetailsResponse.getEnvDetails());

      String result = envServiceConnector.getBaseUrlForLinkInEmail();

      assertNotNull(result);
      assertEquals(
          TestData.getEnvDetailsResponse()
              .getEnvDetails()
              .getLast()
              .getMapValue()
              .get("development"),
          result);
    }
  }

  @Test
  void testGetBaseUrlForLinkInEmail_Production() {
    when(environment.matchesProfiles("development")).thenReturn(false);

    try (MockedStatic<AppEnvProperty> mockedStatic = mockStatic(AppEnvProperty.class)) {
      mockedStatic
          .when(() -> AppEnvProperty.getEnvDetailsList("authsvc", true))
          .thenReturn(envDetailsResponse.getEnvDetails());

      String result = envServiceConnector.getBaseUrlForLinkInEmail();

      assertNotNull(result);
      assertEquals(
          TestData.getEnvDetailsResponse()
              .getEnvDetails()
              .getLast()
              .getMapValue()
              .get("production"),
          result);
    }
  }
}
