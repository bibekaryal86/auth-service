package auth.service.app.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
public class ProfileEntity extends BaseEntity {
  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "phone")
  private String phone;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "is_validated", nullable = false)
  private Boolean isValidated;

  @Column(name = "login_attempts")
  private Integer loginAttempts;

  @Column(name = "last_login")
  private LocalDateTime lastLogin;

  @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
  private ProfileAddressEntity profileAddress;
}
