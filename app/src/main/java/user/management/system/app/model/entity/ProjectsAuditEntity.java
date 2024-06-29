package user.management.system.app.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "projects_audit")
public class ProjectsAuditEntity {
  @Id
  private Integer id;
  private Integer updatedBy;
  private LocalDateTime updatedAt;
  private Integer projectId;
  private String action;
  private String details;
}
