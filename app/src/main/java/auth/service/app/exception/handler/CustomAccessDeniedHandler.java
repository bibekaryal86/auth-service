package auth.service.app.exception.handler;

import auth.service.app.util.ConstantUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
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

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AccessDeniedException accessDeniedException)
      throws IOException, ServletException {
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ResponseMetadata responseMetadata;
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      responseMetadata =
          new ResponseMetadata(
              new ResponseMetadata.ResponseStatusInfo(
                  "Profile is not authenticated to access this resource..."),
              ResponseMetadata.emptyResponseCrudInfo(),
              ResponseMetadata.emptyResponsePageInfo());
    } else {
      responseMetadata =
          new ResponseMetadata(
              new ResponseMetadata.ResponseStatusInfo(
                  "Profile is not authorized to access this resource..."),
              ResponseMetadata.emptyResponseCrudInfo(),
              ResponseMetadata.emptyResponsePageInfo());
    }

    final String jsonResponse =
        ConstantUtils.GSON.toJson(new ResponseWithMetadata(responseMetadata));
    response.getWriter().write(jsonResponse);
  }
}
