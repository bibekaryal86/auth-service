package auth.service.app.exception.handler;

import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.ResponseStatusInfo;
import auth.service.app.util.CommonUtils;
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
          ResponseMetadata.builder()
              .responseStatusInfo(
                  ResponseStatusInfo.builder()
                      .errMsg("Profile is not authenticated to access this resource...")
                      .build())
              .responsePageInfo(CommonUtils.emptyResponsePageInfo())
              .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
              .build();
    } else {
      responseMetadata =
          ResponseMetadata.builder()
              .responseStatusInfo(
                  ResponseStatusInfo.builder()
                      .errMsg("Profile is not authorized to access this resource...")
                      .build())
              .responsePageInfo(CommonUtils.emptyResponsePageInfo())
              .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
              .build();
    }

    final String jsonResponse = CommonUtils.GSON.toJson(responseMetadata);
    response.getWriter().write(jsonResponse);
  }
}
