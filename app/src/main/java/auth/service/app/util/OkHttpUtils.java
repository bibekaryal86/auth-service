package auth.service.app.util;

import io.micrometer.common.util.StringUtils;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.lang.NonNull;

public class OkHttpUtils {

  private static final OkHttpClient okHttpClient =
      new OkHttpClient.Builder()
          .connectTimeout(5, TimeUnit.SECONDS)
          .readTimeout(15, TimeUnit.SECONDS)
          .writeTimeout(15, TimeUnit.SECONDS)
          .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
          .addInterceptor(new InterceptorLoggingUtilsOutgoing())
          .build();

  public static HttpResponse sendRequest(
      final String url,
      final String method,
      final String requestBody,
      final Map<String, String> headers,
      final String authorization) {
    Request request = buildRequest(url, method, requestBody, headers, authorization);
    try (Response response = okHttpClient.newCall(request).execute()) {
      int responseCode = response.code();
      String responseBody = response.body() == null ? "" : response.body().string();
      return new HttpResponse(responseCode, responseBody);
    } catch (Exception ex) {
      Map<String, String> errorMap = Map.of("errMsg", ex.getMessage());
      return new HttpResponse(-1, CommonUtils.getGson().toJson(errorMap));
    }
  }

  private static Request buildRequest(
      final String url,
      final String method,
      final String requestBody,
      final Map<String, String> headers,
      final String authorization) {
    RequestBody body =
        StringUtils.isEmpty(requestBody)
            ? null
            : RequestBody.create(requestBody, MediaType.parse("application/json"));
    Request.Builder requestBuilder = new Request.Builder().url(url).method(method, body);

    if (authorization != null && !authorization.isEmpty()) {
      requestBuilder.header("Authorization", authorization);
    }

    if (headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        requestBuilder.header(entry.getKey(), entry.getValue());
      }
    }

    return requestBuilder.build();
  }

  public static String createBasicAuthHeader(
      @NonNull final String username, @NonNull final String password) {
    String credentials = username + ":" + password;
    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
  }

  public record HttpResponse(int statusCode, String responseBody) {}
}
