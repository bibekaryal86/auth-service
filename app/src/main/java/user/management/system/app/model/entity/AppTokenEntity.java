package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_token")
@Getter
@Setter
@NoArgsConstructor
public class AppTokenEntity extends EntityBaseDates {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "app_user_id")
  private AppUserEntity user;

  @Column(name = "access_token", unique = true)
  private String accessToken;

  @Column(name = "refresh_token", unique = true)
  private String refreshToken;
}
