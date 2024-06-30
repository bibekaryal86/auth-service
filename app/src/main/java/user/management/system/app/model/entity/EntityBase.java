package user.management.system.app.model.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;

public class EntityBase {
  @Id
  private Integer id;
  private LocalDateTime created;
  private LocalDateTime updated;
  private LocalDateTime deleted;
}
