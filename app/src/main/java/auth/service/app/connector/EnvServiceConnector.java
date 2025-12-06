package auth.service.app.connector;

import io.github.bibekaryal86.shdsvc.AppEnvProperty;
import io.github.bibekaryal86.shdsvc.dtos.EnvDetailsResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnvServiceConnector {

  private final Environment environment;

  public List<EnvDetailsResponse.EnvDetails> getAuthServiceEnvProperties() {
    return AppEnvProperty.getEnvDetailsList("authsvc", Boolean.TRUE);
  }

  @Cacheable("redirectUrls")
  public Map<String, String> getRedirectUrls() {
    final boolean isDevelopment = environment.matchesProfiles("sandbox");
    final String envDetailsName =
        String.format("redirectUrls_%s", isDevelopment ? "sandbox" : "production");
    final List<EnvDetailsResponse.EnvDetails> envDetails = getAuthServiceEnvProperties();
    EnvDetailsResponse.EnvDetails withRedirectUrls =
        envDetails.stream()
            .filter(envDetail -> envDetail.getName().equals(envDetailsName))
            .findFirst()
            .orElse(null);
    if (withRedirectUrls == null) {
      return Collections.emptyMap();
    }
    return withRedirectUrls.getMapValue();
  }

  @CacheEvict("redirectUrls")
  public void evictRedirectUrlCache() {}

  @CacheEvict("baseUrlForLinkInEmail")
  public void evictBaseUrlForLinkInEmailCache() {}

  @Cacheable("baseUrlForLinkInEmail")
  public String getBaseUrlForLinkInEmail() {
    final boolean isDevelopment = environment.matchesProfiles("sandbox");
    final String envDetailsName = "baseUrlForLinkInEmail";
    final List<EnvDetailsResponse.EnvDetails> envDetails = getAuthServiceEnvProperties();
    EnvDetailsResponse.EnvDetails withRedirectUrls =
        envDetails.stream()
            .filter(envDetail -> envDetail.getName().equals(envDetailsName))
            .findFirst()
            .orElse(null);
    if (withRedirectUrls == null) {
      return null;
    }
    return withRedirectUrls.getMapValue().get(isDevelopment ? "sandbox" : "production");
  }
}
