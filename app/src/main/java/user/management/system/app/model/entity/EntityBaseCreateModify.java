package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class EntityBaseCreateModify {
  @Column(name = "created_date", nullable = false)
  private LocalDateTime createdDate;

  @Column(name = "updated_date", nullable = false)
  private LocalDateTime updatedDate;
}
