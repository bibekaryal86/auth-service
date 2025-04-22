package auth.service.app.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.model.client.EnvDetailsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import helper.FixtureReader;
import helper.ObjectMapperProvider;
import helper.TestData;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Disabled
public class EnvServiceConnectorTest extends BaseTest {

  private EnvServiceConnector envServiceConnector;
  private MockWebServer server;
  private Environment environment;
  private final String responseJsonFileName = "env-service_getPropertiesResponse.json";

  @BeforeEach
  void setUp() throws Exception {
    server = new MockWebServer();
    server.start(0);
    String getPropertiesUrl = String.format("%s/getProperties", server.url("/"));
    environment = mock(Environment.class);
    envServiceConnector = new EnvServiceConnector(getPropertiesUrl, environment);
  }

  @AfterEach
  void tearDown() throws Exception {
    server.shutdown();
    envServiceConnector.evictRedirectUrlCache();
    envServiceConnector.evictBaseUrlForLinkInEmailCache();
  }

  @Test
  void testGetRedirectUrls_Development() {
    when(environment.matchesProfiles("development")).thenReturn(true);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(Objects.requireNonNull(FixtureReader.readFixture(responseJsonFileName))));
    Map<String, String> result = envServiceConnector.getRedirectUrls();

    assertNotNull(result);
    assertEquals(TestData.getEnvDetailsResponse().getEnvDetails().getFirst().getMapValue(), result);
  }

  @Test
  void testGetRedirectUrls_Production() {
    when(environment.matchesProfiles("development")).thenReturn(false);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(Objects.requireNonNull(FixtureReader.readFixture(responseJsonFileName))));
    Map<String, String> result = envServiceConnector.getRedirectUrls();

    assertNotNull(result);
    assertEquals(TestData.getEnvDetailsResponse().getEnvDetails().get(1).getMapValue(), result);
  }

  @Test
  void testGetRedirectUrls_Empty() throws JsonProcessingException {
    when(environment.matchesProfiles("development")).thenReturn(true);
    EnvDetailsResponse envDetailsResponse = TestData.getEnvDetailsResponse();
    envDetailsResponse.getEnvDetails().removeFirst();
    String jsonString = ObjectMapperProvider.objectMapper().writeValueAsString(envDetailsResponse);

    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(jsonString));
    Map<String, String> result = envServiceConnector.getRedirectUrls();
    assertNotNull(result);
    assertEquals(Collections.emptyMap(), result);
  }

  @Test
  void testGetBaseUrlForLinkInEmail_Development() {
    when(environment.matchesProfiles("development")).thenReturn(true);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(Objects.requireNonNull(FixtureReader.readFixture(responseJsonFileName))));
    String result = envServiceConnector.getBaseUrlForLinkInEmail();

    assertNotNull(result);
    assertEquals(
        TestData.getEnvDetailsResponse().getEnvDetails().getLast().getMapValue().get("development"),
        result);
  }

  @Test
  void testGetBaseUrlForLinkInEmail_Production() {
    when(environment.matchesProfiles("development")).thenReturn(false);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(Objects.requireNonNull(FixtureReader.readFixture(responseJsonFileName))));
    String result = envServiceConnector.getBaseUrlForLinkInEmail();

    assertNotNull(result);
    assertEquals(
        TestData.getEnvDetailsResponse().getEnvDetails().getLast().getMapValue().get("production"),
        result);
  }

  @Test
  void testGetBaseUrlForLinkInEmail_Null() throws JsonProcessingException {
    when(environment.matchesProfiles("development")).thenReturn(true);
    EnvDetailsResponse envDetailsResponse = TestData.getEnvDetailsResponse();
    envDetailsResponse.getEnvDetails().removeLast();
    String jsonString = ObjectMapperProvider.objectMapper().writeValueAsString(envDetailsResponse);

    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(jsonString));
    String result = envServiceConnector.getBaseUrlForLinkInEmail();
    assertNull(result);
  }
}
