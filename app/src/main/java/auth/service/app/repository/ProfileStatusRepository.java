package auth.service.app.repository;

import auth.service.app.model.entity.ProfileStatusEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileStatusRepository extends JpaRepository<ProfileStatusEntity, Long> {
  Optional<ProfileStatusEntity> findByStatusName(final String statusName);
}
