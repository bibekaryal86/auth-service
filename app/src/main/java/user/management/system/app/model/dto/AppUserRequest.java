package user.management.system.app.model.dto;

import java.util.List;
import java.util.Objects;

public class AppUserRequest {
  private String app;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private String password;
  private String status;

  private boolean guestUser;
  private List<AppUserAddressDto> addresses;

  // No Args Constructor
  public AppUserRequest() {}

  // All Args Constructor
  public AppUserRequest(
      final String app,
      final String firstName,
      final String lastName,
      final String email,
      final String phone,
      final String password,
      final String status,
      final boolean guestUser,
      final List<AppUserAddressDto> addresses) {
    this.app = app;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = phone;
    this.password = password;
    this.status = status;
    this.guestUser = guestUser;
    this.addresses = addresses;
  }

  // Getters and Setters
  public String getApp() {
    return this.app;
  }

  public void setApp(final String app) {
    this.app = app;
  }

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

  public String getStatus() {
    return this.status;
  }

  public void setStatus(final String status) {
    this.status = status;
  }

  public boolean isGuestUser() {
    return this.guestUser;
  }

  public void setGuestUser(final boolean guestUser) {
    this.guestUser = guestUser;
  }

  public List<AppUserAddressDto> getAddresses() {
    return this.addresses;
  }

  public void setAddresses(final List<AppUserAddressDto> addresses) {
    this.addresses = addresses;
  }

  // Equals
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AppUserRequest that)) return false;
    return Objects.equals(this.app, that.app)
        && Objects.equals(this.firstName, that.firstName)
        && Objects.equals(this.lastName, that.lastName)
        && Objects.equals(this.email, that.email)
        && Objects.equals(this.phone, that.phone)
        && Objects.equals(this.password, that.password)
        && Objects.equals(this.status, that.status)
        && Objects.equals(this.guestUser, that.guestUser)
        && Objects.equals(this.addresses, that.addresses);
  }

  // HashCode
  @Override
  public int hashCode() {
    return Objects.hash(
        this.app,
        this.firstName,
        this.lastName,
        this.email,
        this.phone,
        this.password,
        this.status,
        this.guestUser,
        this.addresses);
  }

  // ToString
  @Override
  public String toString() {
    return "UserRequest{"
        + "app='"
        + this.app
        + '\''
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
        + ", status='"
        + this.status
        + '\''
        + ", guestUser='"
        + this.guestUser
        + '\''
        + ", addressRequests='"
        + this.addresses
        + '\''
        + "}";
  }
}
