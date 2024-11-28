package auth.service.app.config;

import auth.service.app.connector.AuthenvServiceConnector;
import auth.service.app.service.AppRoleService;
import auth.service.app.service.AppsService;
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
  private final AuthenvServiceConnector authenvServiceConnector;
  private final AppsService appsService;
  private final AppRoleService appRoleService;

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

    CompletableFuture.runAsync(authenvServiceConnector::getRedirectUrls);
    CompletableFuture.runAsync(appsService::readApps);
    CompletableFuture.runAsync(appRoleService::readAppRoles);
  }
}
