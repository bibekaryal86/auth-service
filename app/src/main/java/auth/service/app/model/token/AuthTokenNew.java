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
public class AuthTokenNew {
  private Long platformId;
  private AuthTokenProfile profile;
  private List<AuthTokenRoleNew> roles;
  private List<AuthTokenPermissionNew> permissions;
}
