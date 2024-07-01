package user.management.system.app.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EntityBase {
  @Id
  private Integer id;
  private LocalDateTime created;
  private LocalDateTime updated;
  private LocalDateTime deleted;
}
