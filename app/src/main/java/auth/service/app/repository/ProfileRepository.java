package auth.service.app.repository;

import auth.service.app.model.entity.ProfileEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {

  @Query("SELECT p FROM ProfileEntity p WHERE :isIncludeDeleted = true OR p.deletedDate IS NULL")
  List<ProfileEntity> findAllProfiles(@Param("isIncludeDeleted") final boolean isIncludeDeleted);

  Optional<ProfileEntity> findByEmail(final String email);
}
