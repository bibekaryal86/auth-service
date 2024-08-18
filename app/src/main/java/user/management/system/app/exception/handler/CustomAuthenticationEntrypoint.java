package user.management.system.app.exception.handler;

import static user.management.system.app.util.CommonUtils.convertResponseStatusInfoToJson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import user.management.system.app.model.dto.ResponseStatusInfo;

@Component
public class CustomAuthenticationEntrypoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AuthenticationException authException)
      throws IOException, ServletException {

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder()
            .errMsg("User not authenticated to access this resource...")
            .build();
    final String jsonResponse = convertResponseStatusInfoToJson(responseStatusInfo);
    response.getWriter().write(jsonResponse);
  }
}
