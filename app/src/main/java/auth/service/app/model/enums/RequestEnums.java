package auth.service.app.model.enums;

import lombok.Getter;

public class RequestEnums {
  public enum SortDirection {
    ASC,
    DESC
  }

  @Getter
  public enum FilterOperation {
    EQ("eq"),
    NQ("neq"),
    GT("gt"),
    LT("lt"),
    GTE("gte"),
    LTE("lte");

    private final String operation;

    FilterOperation(String operation) {
      this.operation = operation;
    }
  }
}
