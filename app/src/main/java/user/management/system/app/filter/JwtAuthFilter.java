package user.management.system.app.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import user.management.system.app.exception.JwtInvalidException;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.token.AuthToken;
import user.management.system.app.util.JwtUtils;

public class JwtAuthFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      @NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain)
      throws ServletException, IOException {
    String authorizationHeader = request.getHeader("Authorization");

    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      String token = authorizationHeader.substring(7);

      try {
        AuthToken authToken = JwtUtils.decodeAuthCredentials(token);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(authToken, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JwtInvalidException ex) {
        sendUnauthorizedResponse(response, ex.getMessage());
        return; // Stop further processing
      }
    }

    filterChain.doFilter(request, response);
  }

  private void sendUnauthorizedResponse(final HttpServletResponse response, String errMsg)
      throws IOException {
    final ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(errMsg).build();

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    final String jsonResponse = convertResponseStatusInfoToJson(responseStatusInfo);
    response.getWriter().write(jsonResponse);
  }

  private String convertResponseStatusInfoToJson(final ResponseStatusInfo responseStatusInfo) {
    return "{"
        + "\"message\":\""
        + escapeJson(responseStatusInfo.getMessage())
        + "\","
        + "\"errMsg\":\""
        + escapeJson(responseStatusInfo.getErrMsg())
        + "\""
        + "}";
  }

  private String escapeJson(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
  }
}
