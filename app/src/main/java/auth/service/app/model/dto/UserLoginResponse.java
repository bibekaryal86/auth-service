package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {
  @ToString.Exclude private String aToken;
  @ToString.Exclude private String rToken;
  private AppUserDto user;
  private ResponseStatusInfo responseStatusInfo;
}
