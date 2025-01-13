package auth.service.app.util;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class InterceptorLoggingUtilsOutgoing implements Interceptor {

  private static final Logger log = LoggerFactory.getLogger(InterceptorLoggingUtilsOutgoing.class);

  @NotNull
  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    log.info("Request to [{}] [{}] [{}]", request.method(), request.url(), request.body() == null ? 0 : request.body().contentLength());
    Response response = chain.proceed(request);
    log.info("Response [{}] from [{}] [{}] [{}]", response.code(), request.method(), request.url(), response.body() == null ? 0 : response.body().contentLength());
    return response;
  }
}
