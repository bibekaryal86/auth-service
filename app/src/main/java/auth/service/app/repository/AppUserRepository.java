package auth.service.app.repository;

import auth.service.app.model.entity.AppUserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUserEntity, Integer> {
  Optional<AppUserEntity> findByEmail(final String email);
}
