package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class Project extends EntityBaseNameDescCreateModify {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "status_id", nullable = false)
  private ProjectStatus status;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "repo")
  private String repo;

  @Column(name = "link")
  private String link;

  @Column(name = "deleted_date")
  private LocalDateTime deletedDate;
}
