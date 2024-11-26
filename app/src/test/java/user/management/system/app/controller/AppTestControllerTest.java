package user.management.system.app.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import user.management.system.BaseTest;

public class AppTestControllerTest extends BaseTest {

  @MockitoBean private CacheManager cacheManager;

  @Test
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

  @Test
  public void testResetCaches() {
    Cache cacheMock = mock(Cache.class);
    when(cacheManager.getCacheNames()).thenReturn(List.of("cache1", "cache2"));
    when(cacheManager.getCache("cache1")).thenReturn(cacheMock);
    when(cacheManager.getCache("cache2")).thenReturn(cacheMock);

    webTestClient
        .get()
        .uri("/tests/reset")
        .header("Authorization", "Basic " + basicAuthCredentialsForTest)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("{\"reset\": \"successful\"}");

    verify(cacheMock, times(2)).clear();
  }

  @Test
  public void testResetCaches_FailureForNoAuth() {
    webTestClient.get().uri("/tests/reset").exchange().expectStatus().isUnauthorized();
  }

  @Test
  public void testResetCaches_FailureForBadAuth() {
    webTestClient
        .get()
        .uri("/tests/reset")
        .header("Authorization", "Basic someBase64EncodedString")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
