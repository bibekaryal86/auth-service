package user.management.system.app.model.dto;

import lombok.Getter;

public class RequestSortFilterOptions {

    public enum SortDirection {
        ASC,
        DESC
    }

    @Getter
    public enum FilterOperation {
        EQUAL_TO("eq"),
        GREATER_THAN("gt"),
        LESS_THAN("lt"),
        GREATER_THAN_OR_EQUAL_TO("gte"),
        LESS_THAN_OR_EQUAL_TO("lte");

        private final String operation;

        FilterOperation(String operation) {
            this.operation = operation;
        }
    }
}
