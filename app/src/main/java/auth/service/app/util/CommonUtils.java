package auth.service.app.util;

import static auth.service.app.util.ConstantUtils.INTERNAL_SERVER_ERROR_MESSAGE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import auth.service.app.exception.CheckPermissionException;
import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.exception.JwtInvalidException;
import auth.service.app.exception.ProfileForbiddenException;
import auth.service.app.exception.ProfileLockedException;
import auth.service.app.exception.ProfileNotActiveException;
import auth.service.app.exception.ProfileNotAuthorizedException;
import auth.service.app.exception.ProfileNotValidatedException;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.dto.ResponseCrudInfo;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.ResponsePageInfo;
import auth.service.app.model.dto.ResponseStatusInfo;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.token.AuthToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

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
      return NOT_FOUND;
    } else if (exception instanceof ElementMissingException) {
      return BAD_REQUEST;
    } else if (exception instanceof ProfileForbiddenException
        || exception instanceof ProfileNotValidatedException
        || exception instanceof ElementNotActiveException
        || exception instanceof ProfileNotActiveException
        || exception instanceof ProfileLockedException
        || exception instanceof CheckPermissionException) {
      return FORBIDDEN;
    } else if (exception instanceof ProfileNotAuthorizedException
        || exception instanceof JwtInvalidException) {
      return UNAUTHORIZED;
    } else {
      return INTERNAL_SERVER_ERROR;
    }
  }

  public static <T> HttpStatus getHttpStatusForSingleResponse(final T object) {
    return ObjectUtils.isEmpty(object) ? INTERNAL_SERVER_ERROR : OK;
  }

  public static <T> ResponseStatusInfo getResponseStatusInfoForSingleResponse(final T object) {
    return ObjectUtils.isEmpty(object)
        ? ResponseStatusInfo.builder().errMsg(INTERNAL_SERVER_ERROR_MESSAGE).build()
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

  public static ResponseStatusInfo emptyResponseStatusInfo() {
    return ResponseStatusInfo.builder().errMsg("").build();
  }

  public static ResponsePageInfo emptyResponsePageInfo() {
    return ResponsePageInfo.builder().totalItems(0).totalPages(0).pageNumber(0).perPage(0).build();
  }

  public static ResponseCrudInfo emptyResponseCrudInfo() {
    return ResponseCrudInfo.builder()
        .insertedRowsCount(0)
        .updatedRowsCount(0)
        .deletedRowsCount(0)
        .restoredRowsCount(0)
        .build();
  }

  public static ResponseMetadata emptyResponseMetadata() {
    return ResponseMetadata.builder()
        .responseStatusInfo(emptyResponseStatusInfo())
        .responsePageInfo(emptyResponsePageInfo())
        .responseCrudInfo(emptyResponseCrudInfo())
        .build();
  }

  public static ResponsePageInfo defaultResponsePageInfo(final Page<?> page) {
    return ResponsePageInfo.builder()
        .totalItems((int) page.getTotalElements())
        .totalPages(page.getTotalPages())
        .pageNumber(page.getPageable().getPageNumber())
        .perPage(page.getPageable().getPageSize())
        .build();
  }

  public static ResponseCrudInfo defaultResponseCrudInfo(
      final int inserted, final int updated, final int deleted, final int restored) {
    return ResponseCrudInfo.builder()
        .insertedRowsCount(inserted)
        .updatedRowsCount(updated)
        .deletedRowsCount(deleted)
        .restoredRowsCount(restored)
        .build();
  }

  public static RequestMetadata defaultRequestMetadata(final String sortColumn) {
    return RequestMetadata.builder()
        .isIncludeDeleted(false)
        .isIncludeHistory(false)
        .pageNumber(0)
        .perPage(100)
        .sortColumn(sortColumn)
        .sortDirection(Sort.Direction.ASC)
        .build();
  }

  public static boolean isRequestMetadataIncluded(final RequestMetadata requestMetadata) {
    return requestMetadata != null
        && (requestMetadata.isIncludeDeleted()
            || StringUtils.hasText(requestMetadata.getSortColumn()));
  }

  public static boolean isHistoryToBeIncluded(final RequestMetadata requestMetadata) {
    return requestMetadata != null && requestMetadata.isIncludeHistory();
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

  public static void validatePlatformProfileRoleNotDeleted(
      final PlatformProfileRoleEntity platformProfileRoleEntity) {
    if (platformProfileRoleEntity.getPlatform().getDeletedDate() != null) {
      throw new ElementNotActiveException(
          "Platform", String.valueOf(platformProfileRoleEntity.getPlatform().getId()));
    }
    if (platformProfileRoleEntity.getProfile().getDeletedDate() != null) {
      throw new ElementNotActiveException(
          "Profile", String.valueOf(platformProfileRoleEntity.getProfile().getId()));
    }
  }
}
