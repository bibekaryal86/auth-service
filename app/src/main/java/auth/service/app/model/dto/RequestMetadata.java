package auth.service.app.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import org.springframework.data.domain.Sort;

public class RequestMetadata implements Serializable {
  private Boolean isIncludePermissions;
  private Boolean isIncludePlatforms;
  private Boolean isIncludeProfiles;
  private Boolean isIncludeRoles;
  private Boolean isIncludeDeleted;
  private Boolean isIncludeHistory;
  private Integer pageNumber;
  private Integer perPage;
  private Integer historyPage;
  private Integer historySize;
  private String sortColumn;
  private Sort.Direction sortDirection;

  public RequestMetadata() {}

  @JsonCreator
  public RequestMetadata(
      @JsonProperty("isIncludePermissions") Boolean isIncludePermissions,
      @JsonProperty("isIncludePlatforms") Boolean isIncludePlatforms,
      @JsonProperty("isIncludeProfiles") Boolean isIncludeProfiles,
      @JsonProperty("isIncludeRoles") Boolean isIncludeRoles,
      @JsonProperty("isIncludeDeleted") Boolean isIncludeDeleted,
      @JsonProperty("isIncludeHistory") Boolean isIncludeHistory,
      @JsonProperty("pageNumber") Integer pageNumber,
      @JsonProperty("perPage") Integer perPage,
      @JsonProperty("historyPage") Integer historyPage,
      @JsonProperty("historySize") Integer historySize,
      @JsonProperty("sortColumn") String sortColumn,
      @JsonProperty("sortDirection") Sort.Direction sortDirection) {
    this.isIncludePermissions = Boolean.TRUE.equals(isIncludePermissions);
    this.isIncludePlatforms = Boolean.TRUE.equals(isIncludePlatforms);
    this.isIncludeProfiles = Boolean.TRUE.equals(isIncludeProfiles);
    this.isIncludeRoles = Boolean.TRUE.equals(isIncludeRoles);
    this.isIncludeDeleted = Boolean.TRUE.equals(isIncludeDeleted);
    this.isIncludeHistory = Boolean.TRUE.equals(isIncludeHistory);
    this.pageNumber = pageNumber == null ? 0 : pageNumber;
    this.perPage = perPage == null ? 0 : perPage;
    this.historyPage = historyPage == null ? 0 : historyPage;
    this.historySize = historySize == null ? 0 : historySize;
    this.sortColumn = sortColumn;
    this.sortDirection = sortDirection;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private boolean isIncludePermissions = false;
    private boolean isIncludePlatforms = false;
    private boolean isIncludeProfiles = false;
    private boolean isIncludeRoles = false;
    private boolean isIncludeDeleted = false;
    private boolean isIncludeHistory = false;
    private int pageNumber = 1;
    private int perPage = 100;
    private int historyPage = 1;
    private int historySize = 100;
    private String sortColumn;
    private Sort.Direction sortDirection = Sort.Direction.ASC;

    public Builder isIncludePermissions(boolean value) {
      this.isIncludePermissions = value;
      return this;
    }

    public Builder isIncludePlatforms(boolean value) {
      this.isIncludePlatforms = value;
      return this;
    }

    public Builder isIncludeProfiles(boolean value) {
      this.isIncludeProfiles = value;
      return this;
    }

    public Builder isIncludeRoles(boolean value) {
      this.isIncludeRoles = value;
      return this;
    }

    public Builder isIncludeDeleted(boolean value) {
      this.isIncludeDeleted = value;
      return this;
    }

    public Builder isIncludeHistory(boolean value) {
      this.isIncludeHistory = value;
      return this;
    }

    public Builder pageNumber(int value) {
      this.pageNumber = value;
      return this;
    }

    public Builder perPage(int value) {
      this.perPage = value;
      return this;
    }

    public Builder historyPage(int value) {
      this.historyPage = value;
      return this;
    }

    public Builder historySize(int value) {
      this.historySize = value;
      return this;
    }

    public Builder sortColumn(String value) {
      this.sortColumn = value;
      return this;
    }

    public Builder sortDirection(Sort.Direction value) {
      this.sortDirection = value;
      return this;
    }

    public RequestMetadata build() {
      return new RequestMetadata(
          isIncludePermissions,
          isIncludePlatforms,
          isIncludeProfiles,
          isIncludeRoles,
          isIncludeDeleted,
          isIncludeHistory,
          pageNumber,
          perPage,
          historyPage,
          historySize,
          sortColumn,
          sortDirection);
    }
  }

  public boolean isIncludePermissions() {
    return isIncludePermissions;
  }

  public boolean isIncludePlatforms() {
    return isIncludePlatforms;
  }

  public boolean isIncludeProfiles() {
    return isIncludeProfiles;
  }

  public boolean isIncludeRoles() {
    return isIncludeRoles;
  }

  public boolean isIncludeDeleted() {
    return isIncludeDeleted;
  }

  public boolean isIncludeHistory() {
    return isIncludeHistory;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public int getPerPage() {
    return perPage;
  }

  public int getHistoryPage() {
    return historyPage;
  }

  public int getHistorySize() {
    return historySize;
  }

  public String getSortColumn() {
    return sortColumn;
  }

  public Sort.Direction getSortDirection() {
    return sortDirection;
  }

  public void setIncludePermissions(boolean includePermissions) {
    isIncludePermissions = includePermissions;
  }

  public void setIncludePlatforms(boolean includePlatforms) {
    isIncludePlatforms = includePlatforms;
  }

  public void setIncludeProfiles(boolean includeProfiles) {
    isIncludeProfiles = includeProfiles;
  }

  public void setIncludeRoles(boolean includeRoles) {
    isIncludeRoles = includeRoles;
  }

  public void setIncludeDeleted(boolean includeDeleted) {
    isIncludeDeleted = includeDeleted;
  }

  public void setIncludeHistory(boolean includeHistory) {
    isIncludeHistory = includeHistory;
  }

  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

  public void setPerPage(int perPage) {
    this.perPage = perPage;
  }

  public void setHistoryPage(int historyPage) {
    this.historyPage = historyPage;
  }

  public void setHistorySize(int historySize) {
    this.historySize = historySize;
  }

  public void setSortColumn(String sortColumn) {
    this.sortColumn = sortColumn;
  }

  public void setSortDirection(Sort.Direction sortDirection) {
    this.sortDirection = sortDirection;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RequestMetadata that)) return false;
    return Objects.equals(isIncludePermissions, that.isIncludePermissions)
        && Objects.equals(isIncludePlatforms, that.isIncludePlatforms)
        && Objects.equals(isIncludeProfiles, that.isIncludeProfiles)
        && Objects.equals(isIncludeRoles, that.isIncludeRoles)
        && Objects.equals(isIncludeDeleted, that.isIncludeDeleted)
        && Objects.equals(isIncludeHistory, that.isIncludeHistory)
        && Objects.equals(pageNumber, that.pageNumber)
        && Objects.equals(perPage, that.perPage)
        && Objects.equals(historyPage, that.historyPage)
        && Objects.equals(historySize, that.historySize)
        && Objects.equals(sortColumn, that.sortColumn)
        && sortDirection == that.sortDirection;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        isIncludePermissions,
        isIncludePlatforms,
        isIncludeProfiles,
        isIncludeRoles,
        isIncludeDeleted,
        isIncludeHistory,
        pageNumber,
        perPage,
        historyPage,
        historySize,
        sortColumn,
        sortDirection);
  }

  @Override
  public String toString() {
    return "RequestMetadata{"
        + "isIncludePermissions="
        + isIncludePermissions
        + ", isIncludePlatforms="
        + isIncludePlatforms
        + ", isIncludeProfiles="
        + isIncludeProfiles
        + ", isIncludeRoles="
        + isIncludeRoles
        + ", isIncludeDeleted="
        + isIncludeDeleted
        + ", isIncludeHistory="
        + isIncludeHistory
        + ", pageNumber="
        + pageNumber
        + ", perPage="
        + perPage
        + ", historyPage="
        + historyPage
        + ", historySize="
        + historySize
        + ", sortColumn='"
        + sortColumn
        + '\''
        + ", sortDirection="
        + sortDirection
        + '}';
  }
}
