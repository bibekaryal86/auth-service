package auth.service.app.util;

import auth.service.app.exception.CheckPermissionException;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
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

  private static boolean hasPermission(final String permissionName) {
    final AuthToken authToken = getAuthentication();
    return isSuperUser(authToken)
        || authToken.getPermissions().stream()
            .anyMatch(permission -> Objects.equals(permission.getPermissionName(), permissionName));
  }

  public static boolean isSuperUser(final AuthToken authToken) {
    return authToken.getIsSuperUser() != null && authToken.getIsSuperUser() == true;
  }

  public static boolean canReadPermissions() {
    return hasPermission(ConstantUtils.PERMISSION_READ_PERMISSION);
  }

  public static boolean canReadRoles() {
    return hasPermission(ConstantUtils.PERMISSION_READ_ROLE);
  }

  public static boolean canReadPlatforms() {
    return hasPermission(ConstantUtils.PERMISSION_READ_PLATFORM);
  }

  public static boolean canReadProfiles() {
    return hasPermission(ConstantUtils.PERMISSION_READ_PROFILE);
  }
}
