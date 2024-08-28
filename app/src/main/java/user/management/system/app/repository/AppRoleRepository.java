package user.management.system.app.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import user.management.system.app.model.entity.AppRoleEntity;

@Repository
public interface AppRoleRepository extends JpaRepository<AppRoleEntity, Integer> {
  Optional<AppRoleEntity> findByName(final String name);
}
