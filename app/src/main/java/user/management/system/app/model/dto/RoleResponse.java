package user.management.system.app.model.dto;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RoleResponse extends ResponseMetadata {
  private List<RoleDto> roles;

  public RoleResponse(
      final List<RoleDto> roles,
      final ResponseCrudInfo responseCrudInfo,
      final ResponsePageInfo responsePageInfo,
      final ResponseStatusInfo responseStatusInfo) {
    super(responseCrudInfo, responsePageInfo, responseStatusInfo);
    this.roles = roles;
  }
}
