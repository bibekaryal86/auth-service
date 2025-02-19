package auth.service.app.config;

import auth.service.app.connector.EnvServiceConnector;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduleConfig {

  private final CacheManager cacheManager;
  private final EnvServiceConnector envServiceConnector;

  @Scheduled(cron = "0 3 0 * * *")
  protected void recreateAppCaches() throws InterruptedException {
    log.info("Recreating app caches...");
    cacheManager
        .getCacheNames()
        .forEach(
            name -> {
              Cache cache = cacheManager.getCache(name);
              if (cache != null) {
                cache.clear();
              }
            });

    Thread.sleep(5000);

    CompletableFuture.runAsync(envServiceConnector::getRedirectUrls);
    CompletableFuture.runAsync(envServiceConnector::getBaseUrlForLinkInEmail);
  }
}
