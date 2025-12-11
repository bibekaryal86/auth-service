package unit.auth.service.app.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import auth.service.app.connector.EnvServiceConnector;
import io.github.bibekaryal86.shdsvc.dtos.EnvDetailsResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("EnvServiceConnector Unit Tests")
class EnvServiceConnectorTest {

  @Mock private Environment environment;

  private EnvServiceConnector envServiceConnector;
  private List<EnvDetailsResponse.EnvDetails> mockEnvDetails;

  @BeforeEach
  void setUp() {
    mockEnvDetails = new ArrayList<>();
    envServiceConnector = spy(new EnvServiceConnector(environment));
  }

  @Nested
  @DisplayName("getRedirectUrls() tests")
  class GetRedirectUrlsTests {

    @Test
    @DisplayName("Should return redirect URLs for sandbox environment")
    void shouldReturnRedirectUrlsForSandbox() {
      when(environment.matchesProfiles("sandbox")).thenReturn(true);

      Map<String, String> expectedUrls = new HashMap<>();
      expectedUrls.put("app1", "http://localhost:3000");
      expectedUrls.put("app2", "http://localhost:4000");

      EnvDetailsResponse.EnvDetails envDetail = new EnvDetailsResponse.EnvDetails();
      envDetail.setName("redirectUrls_sandbox");
      envDetail.setMapValue(expectedUrls);
      mockEnvDetails.add(envDetail);

      doReturn(mockEnvDetails).when(envServiceConnector).getAuthServiceEnvProperties();

      Map<String, String> result = envServiceConnector.getRedirectUrls();

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals("http://localhost:3000", result.get("app1"));
      assertEquals("http://localhost:4000", result.get("app2"));
    }

    @Test
    @DisplayName("Should return redirect URLs for production environment")
    void shouldReturnRedirectUrlsForProduction() {
      when(environment.matchesProfiles("sandbox")).thenReturn(false);

      Map<String, String> expectedUrls = new HashMap<>();
      expectedUrls.put("app1", "https://prod.example.com");

      EnvDetailsResponse.EnvDetails envDetail = new EnvDetailsResponse.EnvDetails();
      envDetail.setName("redirectUrls_production");
      envDetail.setMapValue(expectedUrls);
      mockEnvDetails.add(envDetail);

      doReturn(mockEnvDetails).when(envServiceConnector).getAuthServiceEnvProperties();

      Map<String, String> result = envServiceConnector.getRedirectUrls();

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("https://prod.example.com", result.get("app1"));
    }

    @Test
    @DisplayName("Should return empty map when redirect URLs not found")
    void shouldReturnEmptyMapWhenNotFound() {
      when(environment.matchesProfiles("sandbox")).thenReturn(true);

      EnvDetailsResponse.EnvDetails envDetail = new EnvDetailsResponse.EnvDetails();
      envDetail.setName("someOtherName");
      mockEnvDetails.add(envDetail);

      doReturn(mockEnvDetails).when(envServiceConnector).getAuthServiceEnvProperties();

      Map<String, String> result = envServiceConnector.getRedirectUrls();

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty map when env properties list is empty")
    void shouldReturnEmptyMapWhenEnvPropertiesEmpty() {
      when(environment.matchesProfiles("sandbox")).thenReturn(true);
      doReturn(Collections.emptyList()).when(envServiceConnector).getAuthServiceEnvProperties();

      Map<String, String> result = envServiceConnector.getRedirectUrls();

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("getBaseUrlForLinkInEmail() tests")
  class GetBaseUrlForLinkInEmailTests {

    @Test
    @DisplayName("Should return sandbox base URL when in sandbox environment")
    void shouldReturnSandboxBaseUrl() {
      when(environment.matchesProfiles("sandbox")).thenReturn(true);

      Map<String, String> urlMap = new HashMap<>();
      urlMap.put("sandbox", "http://localhost:8080");
      urlMap.put("production", "https://prod.example.com");

      EnvDetailsResponse.EnvDetails envDetail = new EnvDetailsResponse.EnvDetails();
      envDetail.setName("baseUrlForLinkInEmail");
      envDetail.setMapValue(urlMap);
      mockEnvDetails.add(envDetail);

      doReturn(mockEnvDetails).when(envServiceConnector).getAuthServiceEnvProperties();

      String result = envServiceConnector.getBaseUrlForLinkInEmail();

      assertNotNull(result);
      assertEquals("http://localhost:8080", result);
    }

    @Test
    @DisplayName("Should return production base URL when in production environment")
    void shouldReturnProductionBaseUrl() {
      when(environment.matchesProfiles("sandbox")).thenReturn(false);

      Map<String, String> urlMap = new HashMap<>();
      urlMap.put("sandbox", "http://localhost:8080");
      urlMap.put("production", "https://prod.example.com");

      EnvDetailsResponse.EnvDetails envDetail = new EnvDetailsResponse.EnvDetails();
      envDetail.setName("baseUrlForLinkInEmail");
      envDetail.setMapValue(urlMap);
      mockEnvDetails.add(envDetail);

      doReturn(mockEnvDetails).when(envServiceConnector).getAuthServiceEnvProperties();

      String result = envServiceConnector.getBaseUrlForLinkInEmail();

      assertNotNull(result);
      assertEquals("https://prod.example.com", result);
    }

    @Test
    @DisplayName("Should return null when base URL not found")
    void shouldReturnNullWhenNotFound() {
      when(environment.matchesProfiles("sandbox")).thenReturn(true);

      EnvDetailsResponse.EnvDetails envDetail = new EnvDetailsResponse.EnvDetails();
      envDetail.setName("someOtherName");
      mockEnvDetails.add(envDetail);

      doReturn(mockEnvDetails).when(envServiceConnector).getAuthServiceEnvProperties();

      String result = envServiceConnector.getBaseUrlForLinkInEmail();

      assertNull(result);
    }

    @Test
    @DisplayName("Should return null when env properties list is empty")
    void shouldReturnNullWhenEnvPropertiesEmpty() {
      when(environment.matchesProfiles("sandbox")).thenReturn(true);
      doReturn(Collections.emptyList()).when(envServiceConnector).getAuthServiceEnvProperties();

      String result = envServiceConnector.getBaseUrlForLinkInEmail();

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("Cache eviction tests")
  class CacheEvictionTests {

    @Test
    @DisplayName("Should evict redirect URL cache without throwing exception")
    void shouldEvictRedirectUrlCache() {
      assertDoesNotThrow(() -> envServiceConnector.evictRedirectUrlCache());
    }

    @Test
    @DisplayName("Should evict base URL cache without throwing exception")
    void shouldEvictBaseUrlForLinkInEmailCache() {
      assertDoesNotThrow(() -> envServiceConnector.evictBaseUrlForLinkInEmailCache());
    }
  }
}
