package user.management.system.app.model.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenUser {
  private String email;
  private String status;
  private boolean isValidated;
  private boolean isDeleted;
}
