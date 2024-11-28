package auth.service.app.controller;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppTestController {

  private final CacheManager cacheManager;

  public AppTestController(final CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @CrossOrigin
  @GetMapping(value = "/tests/ping", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> pingTest() {
    return ResponseEntity.ok("{\"ping\": \"successful\"}");
  }

  @GetMapping(value = "/tests/reset", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> resetCaches() {
    cacheManager
        .getCacheNames()
        .forEach(
            name -> {
              Cache cache = cacheManager.getCache(name);
              if (cache != null) {
                cache.clear();
              }
            });
    return ResponseEntity.ok("{\"reset\": \"successful\"}");
  }
}
