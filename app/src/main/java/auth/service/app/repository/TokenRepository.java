package auth.service.app.repository;

import auth.service.app.model.entity.TokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, Long> {
  Optional<TokenEntity> findByAccessToken(final String accessToken);

  Optional<TokenEntity> findByRefreshToken(final String refreshToken);

  @Modifying
  @Query(
      value = "UPDATE token SET deleted_date = NOW() WHERE profile_id = :profileId",
      nativeQuery = true)
  int setTokensAsDeletedByProfileId(@Param("profileId") Long profileId);
}
