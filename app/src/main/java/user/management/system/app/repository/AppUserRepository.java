package user.management.system.app.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import user.management.system.app.model.entity.AppUserEntity;

@Repository
public interface AppUserRepository extends JpaRepository<AppUserEntity, Integer> {
  Optional<AppUserEntity> findByAppAndEmail(final String app, final String email);

  List<AppUserEntity> findAllByEmailOrderByApp(final String email);
}
