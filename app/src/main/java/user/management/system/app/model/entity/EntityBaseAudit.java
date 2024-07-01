package user.management.system.app.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EntityBaseAudit {
  @Id
  private Integer id;
  private Integer updatedBy;
  private LocalDateTime updatedAt;
  private String action;
  private String details;
}
