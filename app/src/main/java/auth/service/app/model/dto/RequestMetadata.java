package auth.service.app.model.dto;

import auth.service.app.model.enums.RequestEnums;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestMetadata {
  private boolean isIncludeHistory;
  private int pageNumber;
  private int perPage;
  private String sortColumn;
  private RequestEnums.SortDirection sortDirection;
  private List<String> filterColumns;
  private List<String> filterValues;
  private List<RequestEnums.FilterOperation> filterOperations;
}
