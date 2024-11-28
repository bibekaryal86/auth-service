package auth.service.app.model.dto;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AppUserRoleResponse extends ResponseMetadata {
  private List<AppUserRoleDto> usersRoles;

  public AppUserRoleResponse(
      final List<AppUserRoleDto> usersRoles,
      final ResponseCrudInfo responseCrudInfo,
      final ResponsePageInfo responsePageInfo,
      final ResponseStatusInfo responseStatusInfo) {
    super(responseCrudInfo, responsePageInfo, responseStatusInfo);
    this.usersRoles = usersRoles;
  }
}
