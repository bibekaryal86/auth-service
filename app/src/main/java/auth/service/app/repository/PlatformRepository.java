package auth.service.app.repository;

import auth.service.app.model.entity.PlatformEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformRepository extends JpaRepository<PlatformEntity, Long> {

  @Query("SELECT p FROM PlatformEntity p WHERE :isIncludeDeleted = true OR p.deletedDate IS NULL")
  List<PlatformEntity> findAllPlatforms(@Param("isIncludeDeleted") final boolean isIncludeDeleted);
}
