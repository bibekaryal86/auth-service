package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.model.entity.AppUserRoleId;

@Repository
public interface AppUserRoleRepository extends JpaRepository<AppUserRoleEntity, AppUserRoleId> {}
