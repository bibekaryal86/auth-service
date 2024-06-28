package user.management.system.app.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ModelBase implements Serializable {
  private Integer id;
  private LocalDateTime created;
  private LocalDateTime updated;
  private LocalDateTime deleted;
}
