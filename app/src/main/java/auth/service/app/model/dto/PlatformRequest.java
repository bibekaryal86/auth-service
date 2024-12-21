package auth.service.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class PlatformRequest {
  @NotBlank(message = "Name is required")
  private String platformName;

  @NotBlank(message = "Description is required")
  private String platformDesc;
}
