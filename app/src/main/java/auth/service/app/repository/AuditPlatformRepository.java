package auth.service.app.repository;

import auth.service.app.model.entity.AuditPlatformEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditPlatformRepository extends JpaRepository<AuditPlatformEntity, Long> {
  List<AuditPlatformEntity> findByPlatformId(Long platformId);
}
