package user.management.system.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Users extends ModelBaseAudit {
  private UsersBase user;
  private UsersBase details;

  // orm entities
  private Set<UsersAudit> usersAuditSet;
  private Set<UsersAudit> usersAuditSetUpdatedBy;
  private Set<ProjectsAudit> projectsAuditSetUpdatedBy;
  private Set<Roles> roles;
}
