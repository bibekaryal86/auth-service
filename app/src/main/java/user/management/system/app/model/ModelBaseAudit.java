package user.management.system.app.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ModelBaseAudit implements Serializable {
  private Integer id;
  private UsersBase updatedBy;
  private LocalDateTime updatedAt;
  private String action;
}
