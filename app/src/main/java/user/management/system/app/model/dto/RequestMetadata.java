package user.management.system.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestMetadata {
    private Integer entityId;
    private RequestSortConfig sortConfig;
    private RequestFilterConfig filterConfig;
    private Integer pageNumber;
    private Integer perPage;
    private boolean isIncludeDeleted;
    private boolean isIncludeExtra;
    private boolean isIncludeHistory;
}
