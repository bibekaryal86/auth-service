package auth.service.app.model.token;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthToken {
  private String appId;
  private AuthTokenUser user;
  private List<AuthTokenRole> roles;
  private List<AuthTokenPermission> permissions;
}
