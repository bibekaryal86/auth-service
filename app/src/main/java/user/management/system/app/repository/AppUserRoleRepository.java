package user.management.system.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.model.entity.AppUserRoleId;

@Repository
public interface AppUserRoleRepository extends JpaRepository<AppUserRoleEntity, AppUserRoleId> {
  @Query(
      "SELECT aure FROM AppUserRoleEntity aure JOIN aure.appRole ar WHERE aure.id.appRoleId = :roleId ORDER BY ar.name ASC")
  List<AppUserRoleEntity> findByIdAppUserIdOrderByAppRoleNameAsc(@Param("roleId") int roleId);
}
