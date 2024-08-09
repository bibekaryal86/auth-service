package user.management.system.app.model.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectStatusResponse extends ResponseMetadata {
  private List<ProjectStatusDto> projectStatuses;

  public ProjectStatusResponse(
      final List<ProjectStatusDto> projectStatuses,
      final ResponseCrudInfo responseCrudInfo,
      final ResponsePageInfo responsePageInfo,
      final ResponseStatusInfo responseStatusInfo) {
    super(responseCrudInfo, responsePageInfo, responseStatusInfo);
    this.projectStatuses = projectStatuses;
  }
}
