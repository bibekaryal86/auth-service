package user.management.system.app.exception.handler;

import static user.management.system.app.util.CommonUtils.convertResponseStatusInfoToJson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import user.management.system.app.model.dto.ResponseStatusInfo;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AccessDeniedException accessDeniedException)
      throws IOException, ServletException {
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ResponseStatusInfo responseStatusInfo;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      responseStatusInfo =
          ResponseStatusInfo.builder()
              .errMsg("User is not authenticated to access this resource...")
              .build();
    } else {
      responseStatusInfo =
          ResponseStatusInfo.builder()
              .errMsg("User is not authorized to access this resource...")
              .build();
    }

    final String jsonResponse = convertResponseStatusInfoToJson(responseStatusInfo);
    response.getWriter().write(jsonResponse);
  }
}
