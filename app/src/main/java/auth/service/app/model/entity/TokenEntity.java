package auth.service.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "token")
@Getter
@Setter
@NoArgsConstructor
public class TokenEntity extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "platform_id", nullable = false)
  private PlatformEntity platform;

  @ManyToOne
  @JoinColumn(name = "profile_id", nullable = false)
  private ProfileEntity profile;

  @Column(name = "ip_address", nullable = false)
  private String ipAddress;

  @Column(name = "access_token", unique = true)
  private String accessToken;

  @Column(name = "refresh_token", unique = true)
  private String refreshToken;
}
