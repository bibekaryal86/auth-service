package user.management.system.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppTokenRequest {
  @ToString.Exclude private String accessToken;
  @ToString.Exclude private String refreshToken;
}
