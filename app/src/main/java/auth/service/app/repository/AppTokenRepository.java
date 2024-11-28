package auth.service.app.repository;

import auth.service.app.model.entity.AppTokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppTokenRepository extends JpaRepository<AppTokenEntity, Integer> {
  Optional<AppTokenEntity> findByAccessToken(final String accessToken);

  Optional<AppTokenEntity> findByRefreshToken(final String refreshToken);
}
