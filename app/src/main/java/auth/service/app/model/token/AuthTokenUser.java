package auth.service.app.model.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenUser {
  private int id;
  private String email;
  private String status;
  private boolean isValidated;
  private boolean isDeleted;
}
