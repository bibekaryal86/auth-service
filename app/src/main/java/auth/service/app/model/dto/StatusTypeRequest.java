package auth.service.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class StatusTypeRequest {
  @NotBlank(message = "Component is required")
  private String componentName;

  @NotBlank(message = "Name is required")
  private String statusName;

  @NotBlank(message = "Description is required")
  private String statusDesc;
}
