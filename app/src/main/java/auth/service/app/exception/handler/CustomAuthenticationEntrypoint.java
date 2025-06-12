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

    final ResponseWithMetadata responseWithMetadata =
        new ResponseWithMetadata(
            new ResponseMetadata(
                new ResponseMetadata.ResponseStatusInfo(
                    "Profile not authenticated to access this resource..."),
                ResponseMetadata.emptyResponseCrudInfo(),
                ResponseMetadata.emptyResponsePageInfo()));
    final String jsonResponse = ConstantUtils.GSON.toJson(responseWithMetadata);
    response.getWriter().write(jsonResponse);
  }
}
