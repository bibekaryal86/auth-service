package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import user.management.system.app.model.entity.UserRoleEntity;
import user.management.system.app.model.entity.UserRoleId;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleId> {}
