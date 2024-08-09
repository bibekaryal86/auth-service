package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class EntityBaseNameDescCreateModify extends EntityBaseCreateModify {
  @Column(name = "name", unique = true, nullable = false)
  private String name;

  @Column(name = "description", nullable = false)
  private String description;
}
