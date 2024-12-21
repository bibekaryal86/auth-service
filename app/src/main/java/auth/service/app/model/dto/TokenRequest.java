package auth.service.app.model.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class TokenRequest {
  @Positive(message = "REQUIRED")
  private int profileId;

  @ToString.Exclude private String accessToken;
  @ToString.Exclude private String refreshToken;
}
