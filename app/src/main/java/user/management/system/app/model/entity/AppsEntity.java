package user.management.system.app.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "apps")
@Getter
@Setter
@NoArgsConstructor
public class AppsEntity extends EntityBaseNameDesc {
  @Id private String id;
}
