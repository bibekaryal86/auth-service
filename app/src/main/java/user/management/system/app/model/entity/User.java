package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends EntityBaseCreateModify {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "email", unique = true)
  private String email;

  @Column(name = "phone")
  private String phone;

  @Column(name = "password")
  private String password;

  @ManyToOne
  @JoinColumn(name = "status_id", nullable = false)
  private UserStatus status;

  @Column(name = "is_validated", nullable = false)
  private Boolean isValidated;

  @Column(name = "deleted_date")
  private LocalDateTime deletedDate;
}
