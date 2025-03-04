package auth.service.app.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionDto {
  private Long id;
  private LocalDateTime createdDate;
  private LocalDateTime updatedDate;
  private LocalDateTime deletedDate;

  private String permissionName;
  private String permissionDesc;

  private RoleDto role;
  private AuditResponse auditResponse;
}
