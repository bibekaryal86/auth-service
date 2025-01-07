package auth.service.app.repository;

import auth.service.app.model.entity.TokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, Long> {
  Optional<TokenEntity> findByAccessToken(final String accessToken);

  Optional<TokenEntity> findByRefreshToken(final String refreshToken);
}
