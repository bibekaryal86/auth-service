package auth.service.app.integration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("AppTestController Tests")
public class AppTestControllerTest extends BaseTest {

  @MockitoBean private CacheManager cacheManager;

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
    @DisplayName("Reset test failure for no auth")
    public void testResetCaches_FailureForNoAuth() {
      webTestClient.get().uri("/tests/reset").exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Reset test failure for bad auth")
    public void testResetCaches_FailureForBadAuth() {
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
