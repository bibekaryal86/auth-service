package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import auth.service.BaseTest;
import auth.service.app.model.entity.AuditPermissionEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class AuditPermissionRepositoryTest extends BaseTest {

  @Autowired private AuditPermissionRepository auditPermissionRepository;

  @Test
  void testFindByPermissionId() {
    Page<AuditPermissionEntity> auditPermissionEntityPage =
        auditPermissionRepository.findByPermissionId(ID, Pageable.ofSize(10));
    List<AuditPermissionEntity> auditPermissionEntities = auditPermissionEntityPage.toList();
    assertNotNull(auditPermissionEntities);
    assertEquals(3, auditPermissionEntities.size());
    assertEquals(ID, auditPermissionEntities.getFirst().getPermission().getId());
    assertEquals(ID, auditPermissionEntities.getLast().getPermission().getId());
  }
}
