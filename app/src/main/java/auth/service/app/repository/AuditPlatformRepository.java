package auth.service.app.repository;

import auth.service.app.model.entity.AuditPlatformEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditPlatformRepository
    extends JpaRepository<AuditPlatformEntity, Long>,
        JpaSpecificationExecutor<AuditPlatformEntity> {
  Page<AuditPlatformEntity> findByPlatformId(Long platformId, Pageable pageable);
}
