package auth.service.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppsAppUserRequest {
  @NotBlank(message = "AppID is required")
  private String appId;

  @Positive(message = "UserID is required")
  private int userId;
}
