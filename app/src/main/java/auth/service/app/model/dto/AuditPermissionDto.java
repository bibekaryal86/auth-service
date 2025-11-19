package auth.service.app.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuditPermissionDto {
  private final Long id;
  private final String eventType;
  private final String eventDesc;
  private final LocalDateTime createdAt;
  private final ProfileDto createdBy;
  private final String ipAddress;
  private final String userAgent;
  private final PermissionDto eventData;
  private final PermissionDto permission;
}
