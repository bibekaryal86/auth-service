package auth.service.app.connector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bibekaryal86.shdsvc.dtos.EnvDetailsResponse;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class EnvServiceConnectorTest {

  @Mock private Environment environment;

  private EnvServiceConnector envServiceConnector;
  private List<EnvDetailsResponse.EnvDetails> mockEnvDetails;

  @BeforeEach
  void setUp() {
    mockEnvDetails = new ArrayList<>();
    envServiceConnector = spy(new EnvServiceConnector(environment));
  }

  @Test
  void testGetRedirectUrls_Sandbox_Success() {
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
  void testGetRedirectUrls_Production_Success() {
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
  void testGetRedirectUrls_NotFound_ReturnsEmptyMap() {
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
  void testGetRedirectUrls_EmptyList_ReturnsEmptyMap() {
    when(environment.matchesProfiles("sandbox")).thenReturn(true);
    doReturn(Collections.emptyList()).when(envServiceConnector).getAuthServiceEnvProperties();

    Map<String, String> result = envServiceConnector.getRedirectUrls();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testGetBaseUrlForLinkInEmail_Sandbox_Success() {
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
  void testGetBaseUrlForLinkInEmail_Production_Success() {
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
  void testGetBaseUrlForLinkInEmail_NotFound_ReturnsNull() {
    when(environment.matchesProfiles("sandbox")).thenReturn(true);

    EnvDetailsResponse.EnvDetails envDetail = new EnvDetailsResponse.EnvDetails();
    envDetail.setName("someOtherName");
    mockEnvDetails.add(envDetail);

    doReturn(mockEnvDetails).when(envServiceConnector).getAuthServiceEnvProperties();

    String result = envServiceConnector.getBaseUrlForLinkInEmail();

    assertNull(result);
  }

  @Test
  void testGetBaseUrlForLinkInEmail_EmptyList_ReturnsNull() {
    when(environment.matchesProfiles("sandbox")).thenReturn(true);
    doReturn(Collections.emptyList()).when(envServiceConnector).getAuthServiceEnvProperties();

    String result = envServiceConnector.getBaseUrlForLinkInEmail();

    assertNull(result);
  }

  @Test
  void testEvictRedirectUrlCache() {
    assertDoesNotThrow(() -> envServiceConnector.evictRedirectUrlCache());
  }

  @Test
  void testEvictBaseUrlForLinkInEmailCache() {
    assertDoesNotThrow(() -> envServiceConnector.evictBaseUrlForLinkInEmailCache());
  }
}
