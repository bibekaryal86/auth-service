package auth.service.app.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuditResponse {
  private final ResponseMetadata.ResponsePageInfo auditPageInfo;
  private final List<AuditPermissionDto> auditPermissions;
  private final List<AuditRoleDto> auditRoles;
  private final List<AuditPlatformDto> auditPlatforms;
  private final List<AuditProfileDto> auditProfiles;
}
