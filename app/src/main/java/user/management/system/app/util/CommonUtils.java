package user.management.system.app.util;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static user.management.system.app.util.ConstantUtils.INTERNAL_SERVER_ERROR_MESSAGE;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.ResponseStatusInfo;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtils {

  public static String getSystemEnvProperty(final String keyName, final String defaultValue) {
    final String envProperty =
        System.getProperty(keyName) != null ? System.getProperty(keyName) : System.getenv(keyName);
    return envProperty == null ? defaultValue : envProperty;
  }

  public static HttpStatus getHttpStatusForErrorResponse(final Exception exception) {
    if (exception instanceof ElementNotFoundException) {
      return NOT_FOUND;
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
}
