package user.management.system.app.model.entity;

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
@Table(name = "teams")
public class TeamEntity {
  @Id
  private Integer id;
  private String name;
  private String desc;
  private String status;
  private LocalDateTime created;
  private LocalDateTime updated;
  private LocalDateTime deleted;
}
