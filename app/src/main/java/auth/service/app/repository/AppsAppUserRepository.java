package auth.service.app.repository;

import auth.service.app.model.entity.AppsAppUserEntity;
import auth.service.app.model.entity.AppsAppUserId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppsAppUserRepository extends JpaRepository<AppsAppUserEntity, AppsAppUserId> {
  @Query("SELECT a FROM AppsAppUserEntity a WHERE a.app.id=:appId AND a.appUser.email = :email")
  Optional<AppsAppUserEntity> findByAppIdAndAppUserEmail(
      @Param("appId") String appId, @Param("email") String email);

  @Query("SELECT a FROM AppsAppUserEntity a WHERE a.app.id=:appId ORDER BY a.appUser.lastName DESC")
  List<AppsAppUserEntity> findAllByAppIdOrderByAppUserLastNameDesc(@Param("appId") String appId);

  @Query("SELECT a FROM AppsAppUserEntity a WHERE a.appUser.id=:appUserId ORDER BY a.app.name ASC")
  List<AppsAppUserEntity> findAllByAppUserIdOrderByAppNameAsc(@Param("appUserId") int appUserId);
}
