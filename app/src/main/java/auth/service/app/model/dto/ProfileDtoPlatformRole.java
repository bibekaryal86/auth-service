package auth.service.app.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDtoPlatformRole {
  private PlatformDto platform;
  private List<RoleDto> roles;
}
