package auth.service.app.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordUtils {
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public String hashPassword(final String plainPassword) {
    return bCryptPasswordEncoder.encode(plainPassword);
  }

  public boolean verifyPassword(final String plainPassword, final String hashedPassword) {
    return bCryptPasswordEncoder.matches(plainPassword, hashedPassword);
  }
}
