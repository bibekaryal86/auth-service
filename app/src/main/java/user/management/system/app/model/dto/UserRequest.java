package user.management.system.app.model.dto;

import java.util.Objects;

public class UserRequest {
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private transient String password;
  private int statusId;

  // No Args Constructor
  public UserRequest() {}

  // All Args Constructor
  public UserRequest(
      final String firstName,
      final String lastName,
      final String email,
      final String phone,
      final String password,
      final int statusId) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = phone;
    this.password = password;
    this.statusId = statusId;
  }

  // Required Args Constructor
  public UserRequest(
      final String firstName,
      final String lastName,
      final String email,
      final String phone,
      final int statusId) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = phone;
    this.password = "";
    this.statusId = statusId;
  }

  // Getters and Setters
  public String getFirstName() {
    return this.firstName;
  }

  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return this.lastName;
  }

  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public String getPhone() {
    return this.phone;
  }

  public void setPhone(final String phone) {
    this.phone = phone;
  }

  public String getPassword() {
    return this.password;
  }

  public int getStatusId() {
    return this.statusId;
  }

  public void setStatusId(final int statusId) {
    this.statusId = statusId;
  }

  // Equals
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserRequest that)) return false;
    return Objects.equals(this.firstName, that.firstName)
        && Objects.equals(this.lastName, that.lastName)
        && Objects.equals(this.email, that.email)
        && Objects.equals(this.phone, that.phone)
        && Objects.equals(this.password, that.password)
        && Objects.equals(this.statusId, that.statusId);
  }

  // HashCode
  @Override
  public int hashCode() {
    return Objects.hash(
        this.firstName, this.lastName, this.email, this.phone, this.password, this.statusId);
  }

  // ToString
  @Override
  public String toString() {
    return "UserRequest{"
        + "firstName='"
        + this.firstName
        + '\''
        + ", lastName='"
        + this.lastName
        + '\''
        + ", email='"
        + this.email
        + '\''
        + ", phone='"
        + this.phone
        + '\''
        + ", statusId='"
        + this.statusId
        + '\''
        + "}";
  }
}
