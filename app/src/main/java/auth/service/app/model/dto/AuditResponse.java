package auth.service.app.model.dto;

import auth.service.app.model.entity.AuditPermissionEntity;
import auth.service.app.model.entity.AuditPlatformEntity;
import auth.service.app.model.entity.AuditProfileEntity;
import auth.service.app.model.entity.AuditRoleEntity;
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
  private final List<AuditPermissionEntity> auditPermissions;
  private final List<AuditRoleEntity> auditRoles;
  private final List<AuditPlatformEntity> auditPlatforms;
  private final List<AuditProfileEntity> auditProfiles;
}
