package auth.service.app.util;

import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.exception.CheckPermissionException;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtils {

  public static boolean isProduction() {
    return CommonUtilities.getSystemEnvProperty(ConstantUtils.SPRING_PROFILES_ACTIVE)
        .equals(ConstantUtils.ENV_PROD);
  }

  public static String getBaseUrlForLinkInEmail(final HttpServletRequest request) {
    final String scheme = request.getScheme();
    final String serverName = request.getServerName();
    final int serverPort = request.getServerPort();
    final String contextPath = request.getContextPath();

    StringBuilder baseUrl = new StringBuilder();
    baseUrl.append(scheme).append("://").append(serverName);

    if ((scheme.equals("http") && serverPort != 80)
        || (scheme.equals("https") && serverPort != 443)) {
      baseUrl.append(":").append(serverPort);
    }

    baseUrl.append(contextPath);
    return baseUrl.toString();
  }

  public static String getIpAddress(final HttpServletRequest request) {
    String ipAddress = request.getHeader("X-Forwarded-For");
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    }
    return ipAddress;
  }

  public static String getUserAgent(final HttpServletRequest request) {
    return request.getHeader("User-Agent");
  }

  public static ResponseMetadata.ResponseCrudInfo defaultResponseCrudInfo(
      final int inserted, final int updated, final int deleted, final int restored) {
    return new ResponseMetadata.ResponseCrudInfo(inserted, updated, deleted, restored);
  }

  public static AuthToken getAuthentication() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || authentication.getPrincipal() == null
        || !authentication.isAuthenticated()) {
      throw new CheckPermissionException("Profile not authenticated...");
    }

    if (authentication.getCredentials() != null
        && authentication.getCredentials() instanceof AuthToken authToken) {
      return authToken;
    }

    throw new CheckPermissionException("Profile not authorized...");
  }

  public static boolean isSuperUser(final AuthToken authToken) {
    return Objects.equals(authToken.getIsSuperUser(), Boolean.TRUE);
  }

  public static Long getValidId(final String value) {
    try {
      if (!CommonUtilities.isEmpty(value)) {
        long number = Long.parseLong(value.trim());
        if (number > 0) {
          return number;
        }
      }
    } catch (NumberFormatException ignored) {
    }
    return null;
  }
}
