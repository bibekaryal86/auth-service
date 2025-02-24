package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestMetadata {
  private boolean isIncludePermissions;
  private boolean isIncludePlatforms;
  private boolean isIncludeProfiles;
  private boolean isIncludeRoles;
  private boolean isIncludeDeleted;
  private boolean isIncludeHistory;
  private int pageNumber;
  private int perPage;
  private String sortColumn;
  private Sort.Direction sortDirection;
}
