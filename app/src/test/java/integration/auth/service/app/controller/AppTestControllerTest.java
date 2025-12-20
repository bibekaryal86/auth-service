package integration.auth.service.app.controller;

import static auth.service.app.util.ConstantUtils.ENV_SELF_PASSWORD;
import static auth.service.app.util.ConstantUtils.ENV_SELF_USERNAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import integration.BaseTest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Tag("integration")
@DisplayName("AppTestController Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AppTestControllerTest extends BaseTest {

  @MockitoBean private CacheManager cacheManager;

  private String basicAuthCredentialsForTest =
      Base64.getEncoder()
          .encodeToString(
              String.format("%s:%s", ENV_SELF_USERNAME, ENV_SELF_PASSWORD)
                  .getBytes(StandardCharsets.UTF_8));

  @Nested
  @DisplayName("Ping tests")
  class PingTest {

    @Test
    @DisplayName("Ping test success")
    public void testPingTest() {
      webTestClient
          .get()
          .uri("/tests/ping")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(String.class)
          .isEqualTo("{\"ping\": \"successful\"}");
    }
  }

  @Nested
  @DisplayName("Reset tests")
  class ResetTest {

    @Test
    @DisplayName("Reset test success")
    public void testResetCaches() {
      Cache cacheMock = mock(Cache.class);
      when(cacheManager.getCacheNames()).thenReturn(List.of("cache1", "cache2"));
      when(cacheManager.getCache("cache1")).thenReturn(cacheMock);
      when(cacheManager.getCache("cache2")).thenReturn(cacheMock);

      webTestClient
          .get()
          .uri("/tests/reset")
          .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuthCredentialsForTest)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(String.class)
          .isEqualTo("{\"reset\": \"successful\"}");

      verify(cacheMock, times(2)).clear();
    }

    @Test
    @DisplayName("Reset test failure no auth")
    public void testResetCaches_Failure_NoAuth() {
      webTestClient.get().uri("/tests/reset").exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Reset test failure bad auth")
    public void testResetCaches_Failure_BadAuth() {
      webTestClient
          .get()
          .uri("/tests/reset")
          .header(HttpHeaders.AUTHORIZATION, "Basic someBase64EncodedString")
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }
  }
}
