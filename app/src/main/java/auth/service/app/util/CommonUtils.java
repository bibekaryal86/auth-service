package auth.service.app.util;

import auth.service.app.exception.CheckPermissionException;
import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.exception.ProfileForbiddenException;
import auth.service.app.exception.ProfileLockedException;
import auth.service.app.exception.ProfileNotActiveException;
import auth.service.app.exception.ProfileNotAuthorizedException;
import auth.service.app.exception.ProfileNotValidatedException;
import auth.service.app.exception.TokenInvalidException;
import auth.service.app.model.token.AuthToken;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtils {

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

  public static HttpStatus getHttpStatusForErrorResponse(final Exception exception) {
    if (exception instanceof ElementNotFoundException) {
      return HttpStatus.NOT_FOUND;
    } else if (exception instanceof ElementMissingException) {
      return HttpStatus.BAD_REQUEST;
    } else if (exception instanceof ProfileForbiddenException
        || exception instanceof ProfileNotValidatedException
        || exception instanceof ElementNotActiveException
        || exception instanceof ProfileNotActiveException
        || exception instanceof ProfileLockedException
        || exception instanceof CheckPermissionException) {
      return HttpStatus.FORBIDDEN;
    } else if (exception instanceof ProfileNotAuthorizedException
        || exception instanceof TokenInvalidException) {
      return HttpStatus.UNAUTHORIZED;
    } else {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }

  public static <T> HttpStatus getHttpStatusForSingleResponse(final T object) {
    return ObjectUtils.isEmpty(object) ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK;
  }

  public static <T> ResponseMetadata.ResponseStatusInfo getResponseStatusInfoForSingleResponse(
      final T object) {
    return ObjectUtils.isEmpty(object)
        ? new ResponseMetadata.ResponseStatusInfo(ConstantUtils.INTERNAL_SERVER_ERROR_MESSAGE)
        : null;
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

  public static ResponseMetadata.ResponsePageInfo defaultResponsePageInfo(final Page<?> page) {
    return new ResponseMetadata.ResponsePageInfo(
        (int) page.getTotalElements(),
        page.getTotalPages(),
        page.getPageable().getPageNumber() + 1,
        page.getPageable().getPageSize());
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
      if (authToken.getIsSuperUser() == null) {
        authToken.setIsSuperUser(false);
      }
      return authToken;
    }

    throw new CheckPermissionException("Profile not authorized...");
  }

  private static boolean hasPermission(final String permissionName) {
    final AuthToken authToken = getAuthentication();
    return authToken.getIsSuperUser()
        || authToken.getPermissions().stream()
            .anyMatch(permission -> Objects.equals(permission.getPermissionName(), permissionName));
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
