package auth.service.app.repository;

import auth.service.app.model.entity.TokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, Long> {
  Optional<TokenEntity> findByRefreshToken(final String refreshToken);

  @Modifying
  @Transactional
  @Query(
      value =
          "UPDATE token SET deleted_date = NOW() WHERE profile_id = :profileId AND created_date >= NOW() - INTERVAL '1 hour'",
      nativeQuery = true)
  int setTokensAsDeletedByProfileId(@Param("profileId") Long profileId);

  @Deprecated
  @Modifying
  @Transactional
  @Query(
      value =
          "UPDATE token SET deleted_date = CURRENT_TIMESTAMP WHERE profile_id = :profileId AND created_date >= CURRENT_TIMESTAMP - 3600",
      nativeQuery = true)
  int setTokensAsDeletedByProfileIdTest(@Param("profileId") Long profileId);
}
