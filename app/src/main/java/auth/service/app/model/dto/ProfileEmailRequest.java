package auth.service.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class ProfileEmailRequest {
  @NotBlank(message = "Old Email is Required")
  private String oldEmail;

  @NotBlank(message = "New Email is Required")
  private String newEmail;
}
