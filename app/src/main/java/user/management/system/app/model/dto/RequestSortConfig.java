package user.management.system.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestSortConfig {
    private String table;
    private String column;
    private RequestSortFilterOptions.SortDirection direction;
}
