package user.management.system.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestFilterConfig {
    private String table;
    private String column;
    private Object value;
    private RequestSortFilterOptions.FilterOperation operation;
}
