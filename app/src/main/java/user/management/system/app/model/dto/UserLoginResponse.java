package user.management.system.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {
  private String token;
  private AppUserDto user;
  private ResponseStatusInfo responseStatusInfo;
}
