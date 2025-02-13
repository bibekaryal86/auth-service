package auth.service.app.model.dto;

import auth.service.app.model.enums.RequestEnums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestMetadata {
  private boolean isIncludeDeleted;
  private boolean isIncludeHistory;
  private int pageNumber;
  private int perPage;
  private String sortColumn;
  private RequestEnums.SortDirection sortDirection;
  private String filterColumn;
  private String filterValue;
  private RequestEnums.FilterOperation filterOperation;
  private List<String> filterColumns;
  private List<String> filterValues;
  private List<RequestEnums.FilterOperation> filterOperations;
}
