package auth.service.app.repository;

import auth.service.app.model.entity.AuditPlatformEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditPlatformRepository extends JpaRepository<AuditPlatformEntity, Long> {
  List<AuditPlatformEntity> findByPlatformId(Long platformId);

  @Modifying
  @Query("DELETE FROM AuditPlatformEntity a WHERE a.createdAt < :paramDate")
  int deleteByCreatedAtBefore(@Param("paramDate") LocalDateTime paramDate);
}
