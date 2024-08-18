package user.management.system.app.util;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static user.management.system.app.util.ConstantUtils.INTERNAL_SERVER_ERROR_MESSAGE;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import user.management.system.app.exception.ElementMissingException;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.exception.UserForbiddenException;
import user.management.system.app.exception.UserNotAuthorizedException;
import user.management.system.app.exception.UserNotValidatedException;
import user.management.system.app.model.dto.ResponseStatusInfo;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtils {

  public static String getSystemEnvProperty(final String keyName, final String defaultValue) {
    final String envProperty =
        System.getProperty(keyName) != null ? System.getProperty(keyName) : System.getenv(keyName);
    return envProperty == null ? defaultValue : envProperty;
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

  public static HttpStatus getHttpStatusForErrorResponse(final Exception exception) {
    if (exception instanceof ElementNotFoundException) {
      return NOT_FOUND;
    } else if (exception instanceof ElementMissingException) {
      return BAD_REQUEST;
    } else if (exception instanceof UserForbiddenException
        || exception instanceof UserNotValidatedException) {
      return FORBIDDEN;
    } else if (exception instanceof UserNotAuthorizedException) {
      return UNAUTHORIZED;
    } else {
      return SERVICE_UNAVAILABLE;
    }
  }

  public static <T> HttpStatus getHttpStatusForSingleResponse(T object) {
    return ObjectUtils.isEmpty(object) ? INTERNAL_SERVER_ERROR : OK;
  }

  public static <T> ResponseStatusInfo getResponseStatusInfoForSingleResponse(T object) {
    return ObjectUtils.isEmpty(object)
        ? ResponseStatusInfo.builder().errMsg(INTERNAL_SERVER_ERROR_MESSAGE).build()
        : null;
  }

  public static String convertResponseStatusInfoToJson(
      final ResponseStatusInfo responseStatusInfo) {
    return "{"
        + "\"message\":\""
        + escapeJson(responseStatusInfo.getMessage())
        + "\","
        + "\"errMsg\":\""
        + escapeJson(responseStatusInfo.getErrMsg())
        + "\""
        + "}";
  }

  private static String escapeJson(final String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
  }
}
