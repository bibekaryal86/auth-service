package user.management.system.app.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import user.management.system.app.model.entity.AppTokenEntity;

@Repository
public interface AppTokenRepository extends JpaRepository<AppTokenEntity, Integer> {
  Optional<AppTokenEntity> findByAccessToken(final String accessToken);
  Optional<AppTokenEntity> findByRefreshToken(final String refreshToken);
}
