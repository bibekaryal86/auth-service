package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "app_user",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uc_app_user_app_email",
          columnNames = {"app", "email"})
    })
@Getter
@Setter
@NoArgsConstructor
public class AppUserEntity extends EntityBaseDates {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "app", nullable = false)
  private String app;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "phone")
  private String phone;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "is_validated", nullable = false)
  private Boolean isValidated;
}
