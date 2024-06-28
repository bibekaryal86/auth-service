package user.management.system.app.model.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "projects")
public class ProjectsDto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "name", nullable = false, length = 250, unique = true)
  private String name;

  @Column(name = "desc", nullable = false, columnDefinition = "text")
  private String desc;

  @Column(name = "status", nullable = false, length = 50)
  private String status;

  @Column(name = "created", nullable = false)
  private LocalDateTime created;

  @Column(name = "updated", nullable = false)
  private LocalDateTime updated;

  @Column(name = "deleted")
  private LocalDateTime deleted;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
  private Set<ProjectsAuditDto> projectsAuditDtoSet;

  @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
  private List<UsersProjectsRolesDto> userProjectRoles = new ArrayList<>();
}
