package auth.service.app.repository;

import auth.service.app.model.entity.AppRoleEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppRoleRepository extends JpaRepository<AppRoleEntity, Integer> {
  Optional<AppRoleEntity> findByName(final String name);
}
