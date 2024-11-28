package auth.service.app.model.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppTokenRequest {
  @Positive(message = "REQUIRED")
  private int appUserId;

  @ToString.Exclude private String accessToken;
  @ToString.Exclude private String refreshToken;
}
