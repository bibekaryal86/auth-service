package auth.service.app.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieService {

  public String getCookieValue(final HttpServletRequest request, final String name) {
    final Cookie[] cookies = request.getCookies();

    if (cookies == null) {
      return null;
    }

    return Arrays.stream(cookies)
        .filter(cookie -> name.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }

  public ResponseCookie buildRefreshCookie(final String token, final long maxAge) {
    return ResponseCookie.from(ConstantUtils.COOKIE_REFRESH_TOKEN, token)
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .maxAge(maxAge)
        .path("/api/v1/ba_profiles/platform/")
        // .path("/")
        .build();
  }

  public ResponseCookie buildCsrfCookie(final String token, final long maxAge) {
    return ResponseCookie.from(ConstantUtils.COOKIE_CSRF_TOKEN, token)
        .httpOnly(false)
        .secure(true)
        .sameSite("Strict")
        .maxAge(maxAge)
        .path("/")
        .build();
  }
}
