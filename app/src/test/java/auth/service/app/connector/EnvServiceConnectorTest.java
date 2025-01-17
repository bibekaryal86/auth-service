package auth.service.app.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import helper.FixtureReader;
import helper.TestData;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
    WebClient webClient = WebClient.builder().baseUrl(server.url("/").toString()).build();
    environment = mock(Environment.class);
    envServiceConnector = new EnvServiceConnector(getPropertiesUrl, webClient, environment);
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
    assertEquals(TestData.getEnvDetailsResponse().getFirst().getMapValue(), result);
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
    assertEquals(TestData.getEnvDetailsResponse().get(1).getMapValue(), result);
  }

  @Test
  void testGetRedirectUrls_Empty() {
    when(environment.matchesProfiles("development")).thenReturn(true);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("[]"));
    Map<String, String> result = envServiceConnector.getRedirectUrls();
    assertNotNull(result);
    assertEquals(Collections.emptyMap(), result);
  }

  @Test
  void testGetRedirectUrls_Unauthorized() {
    when(environment.matchesProfiles("development")).thenReturn(true);
    server.enqueue(
        new MockResponse()
            .setResponseCode(401)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    assertThrows(
        WebClientResponseException.Unauthorized.class,
        () -> {
          envServiceConnector.getRedirectUrls();
        });
    assertEquals(1, server.getRequestCount());
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
        TestData.getEnvDetailsResponse().getLast().getMapValue().get("development"), result);
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
        TestData.getEnvDetailsResponse().getLast().getMapValue().get("production"), result);
  }

  @Test
  void testGetBaseUrlForLinkInEmail_Null() {
    when(environment.matchesProfiles("development")).thenReturn(true);
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("[]"));
    String result = envServiceConnector.getBaseUrlForLinkInEmail();
    assertNull(result);
  }
}
