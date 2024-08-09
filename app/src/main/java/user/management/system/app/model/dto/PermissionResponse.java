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
public class PermissionResponse extends ResponseMetadata {
  private List<PermissionDto> permissions;

  public PermissionResponse(
      final List<PermissionDto> permissions,
      final ResponseCrudInfo responseCrudInfo,
      final ResponsePageInfo responsePageInfo,
      final ResponseStatusInfo responseStatusInfo) {
    super(responseCrudInfo, responsePageInfo, responseStatusInfo);
    this.permissions = permissions;
  }
}
