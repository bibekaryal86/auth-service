package auth.service.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class AddressTypeRequest {
  @NotBlank(message = "Name is required")
  private String typeName;

  @NotBlank(message = "Description is required")
  private String typeDesc;
}
