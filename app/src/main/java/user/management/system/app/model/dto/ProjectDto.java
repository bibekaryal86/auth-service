package user.management.system.app.model.dto;

import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ProjectDto extends ProjectRequest {
  private Integer id;
  private ProjectStatusDto status;
  private LocalDateTime createdDate;
  private LocalDateTime updatedDate;
  private LocalDateTime deletedDate;

  public ProjectDto() {
    super();
  }

  public ProjectDto(
      final Integer id,
      final String name,
      final String description,
      final LocalDateTime startDate,
      final LocalDateTime endDate,
      final String repo,
      final String link,
      final ProjectStatusDto status,
      final LocalDateTime createdDate,
      final LocalDateTime updatedDate,
      final LocalDateTime deletedDate) {
    super(name, description, 0, startDate, endDate, repo, link);
    this.status = status;
    this.createdDate = createdDate;
    this.updatedDate = updatedDate;
    this.deletedDate = deletedDate;
  }
}
