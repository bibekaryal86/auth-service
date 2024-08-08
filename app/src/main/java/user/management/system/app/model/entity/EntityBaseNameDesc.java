package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class EntityBaseNameDesc {
  @Column(name = "name", unique = true, nullable = false)
  private String name;

  @Column(name = "description", nullable = false)
  private String description;
}
