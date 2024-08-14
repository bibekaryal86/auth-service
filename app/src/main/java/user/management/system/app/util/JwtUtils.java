package user.management.system.app.util;

import static user.management.system.app.util.CommonUtils.getSystemEnvProperty;
import static user.management.system.app.util.ConstantUtils.ENV_SECRET_KEY;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtils {

    private static final String SECRET_KEY = getSystemEnvProperty(ENV_SECRET_KEY, null);
    private static final long EMAIL_LINK_EXPIRATION = 90000; // 15 minutes

    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public static String encodeEmailAddress(final String email, final int minutes) {
        return Jwts.builder()
                .claims(Map.of("email_token", email))
                .expiration(new Date(System.currentTimeMillis() + EMAIL_LINK_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    public static String decodeEmailAddress(String encodedEmail) {
        try {
            String emailToken = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(encodedEmail)
                    .getPayload()
                    .get("email_token", String.class);

            if (emailToken == null) {
                throw new IllegalArgumentException("Incorrect Email Credentials");
            }

            return emailToken;
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("Token has expired", e);
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid Email Credentials", e);
        }
    }
}
