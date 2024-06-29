package user.management.system.app.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity implements Serializable {
  @Id
  private Integer id;
  private String firstName;
  private String lastName;
  private String email;
  private transient String password;
  private String status;
  private LocalDateTime created;
  private LocalDateTime updated;
  private LocalDateTime deleted;
}
