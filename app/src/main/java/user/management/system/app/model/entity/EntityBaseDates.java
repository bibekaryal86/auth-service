package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class EntityBaseDates {
  @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdDate;

  @Column(name = "updated_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime updatedDate;

  @Column(name = "deleted_date")
  private LocalDateTime deletedDate;

  // Set default values for the timestamps on creation
  @PrePersist
  protected void onCreate() {
    this.createdDate = LocalDateTime.now();
    this.updatedDate = LocalDateTime.now();
  }

  // Update the updatedDate timestamp on updates
  @PreUpdate
  protected void onUpdate() {
    this.updatedDate = LocalDateTime.now();
  }
}
