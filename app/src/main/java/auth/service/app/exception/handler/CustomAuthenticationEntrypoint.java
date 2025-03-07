package auth.service.app.exception.handler;

import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.ResponseStatusInfo;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.ConstantUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class CustomAuthenticationEntrypoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AuthenticationException authException)
      throws IOException, ServletException {

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    final ResponseMetadata responseMetadata =
        ResponseMetadata.builder()
            .responseStatusInfo(
                ResponseStatusInfo.builder()
                    .errMsg("Profile not authenticated to access this resource...")
                    .build())
            .responsePageInfo(CommonUtils.emptyResponsePageInfo())
            .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
            .build();
    final String jsonResponse = ConstantUtils.GSON.toJson(responseMetadata);
    response.getWriter().write(jsonResponse);
  }
}
