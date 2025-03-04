package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import auth.service.BaseTest;
import auth.service.app.model.entity.AuditPlatformEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class AuditPlatformRepositoryTest extends BaseTest {

  @Autowired private AuditPlatformRepository auditPlatformRepository;

  @Test
  void testFindByPlatformId() {
    Page<AuditPlatformEntity> auditPlatformEntityPage =
        auditPlatformRepository.findByPlatformId(ID, Pageable.ofSize(10));
    List<AuditPlatformEntity> auditPlatformEntities = auditPlatformEntityPage.toList();
    assertNotNull(auditPlatformEntities);
    assertEquals(3, auditPlatformEntities.size());
    assertEquals(ID, auditPlatformEntities.getFirst().getPlatform().getId());
    assertEquals(ID, auditPlatformEntities.getLast().getPlatform().getId());
  }
}
