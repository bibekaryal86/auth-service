package user.management.system.app.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "projects")
public class ProjectEntity extends EntityBase {
  private String name;
  private String description;
  private String status;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
}
