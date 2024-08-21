package user.management.system.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import user.management.system.app.connector.AuthenvServiceConnector;
import user.management.system.app.service.AppRoleService;
import user.management.system.app.service.AppsService;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduleConfig {

  private final CacheManager cacheManager;
  private final AuthenvServiceConnector authenvServiceConnector;
  private final AppsService appsService;
  private final AppRoleService appRoleService;

  @Scheduled(cron = "0 0 0 * * *")
  protected void putAllCache() throws InterruptedException {
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

    authenvServiceConnector.getRedirectUrls();
    appsService.readApps();
    appRoleService.readAppRoles();
  }
}
