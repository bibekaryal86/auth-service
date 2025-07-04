package auth.service.app.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class InterceptorLoggingUtilsIncoming implements HandlerInterceptor {

  @Override
  public boolean preHandle(
      final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
    request.setAttribute("startTime", System.currentTimeMillis());
    log.info("Receiving [{}] URL [{}]", request.getMethod(), request.getRequestURI());
    return true;
  }

  @Override
  public void afterCompletion(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final Object handler,
      final Exception ex) {
    final long duration = System.currentTimeMillis() - (Long) request.getAttribute("startTime");
    log.info(
        "Returning [{}] Status Code [{}] URL [{}] AFTER [{}ms]",
        request.getMethod(),
        response.getStatus(),
        request.getRequestURI(),
        duration);
  }
}
