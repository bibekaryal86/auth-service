package auth.service.app.model.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenProfile {
  private long id;
  private String email;
  private long statusId;
  private boolean isValidated;
}
