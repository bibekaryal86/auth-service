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
import auth.service.app.model.token.AuthToken;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
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

  public static String convertResponseMetadataToJson(final ResponseMetadata responseMetadata) {
    try {
      return new ObjectMapper().writeValueAsString(responseMetadata);
    } catch (JsonProcessingException e) {
      return "{"
          + "\"errMsg\":\""
          + escapeJson(responseMetadata.getResponseStatusInfo().getErrMsg())
          + "\""
          + "}";
    }
  }

  private static String escapeJson(final String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
  }

  public static Gson getGson() {
    return new GsonBuilder()
        .setExclusionStrategies(
            new ExclusionStrategy() {
              public boolean shouldSkipField(FieldAttributes f) {
                return (f == null);
              }

              public boolean shouldSkipClass(Class<?> clazz) {
                return false;
              }
            })
        .create();
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

  public static ResponsePageInfo defaultResponsePageInfo(final Collection<?> collection) {
    return ResponsePageInfo.builder()
        .totalItems(collection.size())
        .totalPages(1)
        .pageNumber(1)
        .perPage(collection.size())
        .build();
  }

  public static ResponsePageInfo defaultResponsePageInfo(final Page<?> permissionEntityPage) {
    return ResponsePageInfo.builder()
        .totalItems((int) permissionEntityPage.getTotalElements())
        .totalPages(permissionEntityPage.getTotalPages())
        .pageNumber(permissionEntityPage.getNumber())
        .perPage(permissionEntityPage.getSize())
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

  public static <T> Specification<T> getQuerySpecification(final RequestMetadata requestMetadata) {
    Specification<T> specification = Specification.where(null);

    if (!shouldIncludeDeletedRecords(requestMetadata)) {
      specification.and(JpaDataUtils.isFieldNull(ConstantUtils.DELETED_DATE));
    }

    return specification;
  }

  public static Pageable getQueryPageable(
      final RequestMetadata requestMetadata, final String defaultSortColumn) {
    final String sortColumn =
        StringUtils.hasText(requestMetadata.getSortColumn())
            ? requestMetadata.getSortColumn()
            : defaultSortColumn;
    return JpaDataUtils.createPageable(
        requestMetadata.getPageNumber(),
        requestMetadata.getPerPage(),
        sortColumn,
        requestMetadata.getSortDirection());
  }

  private static boolean shouldIncludeDeletedRecords(final RequestMetadata requestMetadata) {
    if (requestMetadata.isIncludeDeleted()) {
      AuthToken authToken =
          (AuthToken) SecurityContextHolder.getContext().getAuthentication().getCredentials();
      return authToken.isSuperUser();
    }
    return false;
  }
}
