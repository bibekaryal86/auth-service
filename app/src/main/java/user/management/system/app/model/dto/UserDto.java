package user.management.system.app.model.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserDto extends UserRequest {
  private Integer id;
  private UserStatusDto status;
  private boolean isValidated;
  private LocalDateTime createdDate;
  private LocalDateTime updatedDate;
  private LocalDateTime deletedDate;

  // No Args Constructor
  public UserDto() {
    super();
  }

  // Required Args Constructor
  public UserDto(
      final Integer id,
      final String firstName,
      final String lastName,
      final String email,
      final String phone,
      final UserStatusDto status,
      final boolean isValidated,
      final LocalDateTime createdDate,
      final LocalDateTime updatedDate,
      final LocalDateTime deletedDate) {
    super(firstName, lastName, email, phone, "", 0);
    this.id = id;
    this.status = status;
    this.isValidated = isValidated;
    this.createdDate = createdDate;
    this.updatedDate = updatedDate;
    this.deletedDate = deletedDate;
  }

  // Getters and Setters
  public Integer getId() {
    return this.id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public UserStatusDto getStatus() {
    return this.status;
  }

  public void setStatus(final UserStatusDto status) {
    this.status = status;
  }

  public boolean isValidated() {
    return this.isValidated;
  }

  public void setValidated(final boolean isValidated) {
    this.isValidated = isValidated;
  }

  public LocalDateTime getCreatedDate() {
    return this.createdDate;
  }

  public void setCreatedDate(final LocalDateTime createdDate) {
    this.createdDate = createdDate;
  }

  public LocalDateTime getUpdatedDate() {
    return this.updatedDate;
  }

  public void setUpdatedDate(final LocalDateTime updatedDate) {
    this.updatedDate = updatedDate;
  }

  public LocalDateTime getDeletedDate() {
    return this.deletedDate;
  }

  public void setDeletedDate(final LocalDateTime deletedDate) {
    this.deletedDate = deletedDate;
  }

  // Equals
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserDto that)) return false;
    return Objects.equals(this.getFirstName(), that.getFirstName())
        && Objects.equals(this.getLastName(), that.getLastName())
        && Objects.equals(this.getEmail(), that.getEmail())
        && Objects.equals(this.getPhone(), that.getPhone())
        && Objects.equals(this.getPassword(), that.getPassword())
        && Objects.equals(this.getStatusId(), that.getStatusId())
        && Objects.equals(this.id, that.id)
        && Objects.equals(this.isValidated, that.isValidated)
        && Objects.equals(this.createdDate, that.createdDate)
        && Objects.equals(this.updatedDate, that.updatedDate)
        && Objects.equals(this.deletedDate, that.deletedDate);
  }

  // HashCode
  @Override
  public int hashCode() {
    return Objects.hash(
        this.getFirstName(),
        this.getLastName(),
        this.getEmail(),
        this.getPhone(),
        this.getPassword(),
        this.getStatusId(),
        this.id,
        this.isValidated,
        this.createdDate,
        this.updatedDate,
        this.deletedDate,
        this.status);
  }

  // ToString
  @Override
  public String toString() {
    return "UserDto{"
        + "id='"
        + this.id
        + '\''
        + "firstName='"
        + this.getFirstName()
        + '\''
        + ", lastName='"
        + this.getLastName()
        + '\''
        + ", email='"
        + this.getEmail()
        + '\''
        + ", phone='"
        + this.getPhone()
        + '\''
        + ", statusId='"
        + this.getStatusId()
        + '\''
        + ", status='"
        + this.status
        + '\''
        + ", isValidated='"
        + this.isValidated
        + '\''
        + ", createdDate='"
        + this.createdDate
        + '\''
        + ", updatedDate='"
        + this.updatedDate
        + '\''
        + ", deletedDate='"
        + this.deletedDate
        + '\''
        + "}";
  }
}
