package user.management.system.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import user.management.system.app.model.entity.AppPermissionEntity;

@Repository
public interface AppPermissionRepository extends JpaRepository<AppPermissionEntity, Integer> {
  List<AppPermissionEntity> findByAppIdOrderByNameAsc(@Param("appId") final String appId);
}
