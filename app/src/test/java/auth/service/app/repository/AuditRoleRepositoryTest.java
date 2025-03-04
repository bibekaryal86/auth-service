package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import auth.service.BaseTest;
import auth.service.app.model.entity.AuditRoleEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class AuditRoleRepositoryTest extends BaseTest {

  @Autowired private AuditRoleRepository auditRoleRepository;

  @Test
  void testFindByRoleId() {
    Page<AuditRoleEntity> auditRoleEntityPage =
        auditRoleRepository.findByRoleId(ID, Pageable.ofSize(10));
    List<AuditRoleEntity> auditRoleEntities = auditRoleEntityPage.toList();
    assertNotNull(auditRoleEntities);
    assertEquals(3, auditRoleEntities.size());
    assertEquals(ID, auditRoleEntities.getFirst().getRole().getId());
    assertEquals(ID, auditRoleEntities.getLast().getRole().getId());
  }
}
