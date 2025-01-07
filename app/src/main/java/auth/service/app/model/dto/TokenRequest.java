package auth.service.app.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class TokenRequest {
  @NotNull(message = "REQUIRED")
  @Min(value = 1, message = "REQUIRED")
  private Long profileId;

  @ToString.Exclude private String accessToken;
  @ToString.Exclude private String refreshToken;
}
