package user.management.system.app.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

@Entity
@Table(name = "user_status")
@Getter
@Setter
@NoArgsConstructor
public class UserStatus extends EntityBaseNameDesc {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
}
