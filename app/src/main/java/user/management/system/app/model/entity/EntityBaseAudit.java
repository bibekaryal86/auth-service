package user.management.system.app.model.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;

public class EntityBaseAudit {
  @Id
  private Integer id;
  private Integer updatedBy;
  private LocalDateTime updatedAt;
  private String action;
  private String details;
}
