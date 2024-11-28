package auth.service.app.repository;

import auth.service.app.model.entity.AppPermissionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppPermissionRepository extends JpaRepository<AppPermissionEntity, Integer> {
  List<AppPermissionEntity> findByAppIdOrderByNameAsc(@Param("appId") final String appId);
}
