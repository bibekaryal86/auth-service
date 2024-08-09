package user.management.system.app.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
  private Integer id;
  private String name;
  private String description;
  private ProjectStatusDto status;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String repo;
  private String link;
  private LocalDateTime createdDate;
  private LocalDateTime updatedDate;
  private LocalDateTime deletedDate;
}
