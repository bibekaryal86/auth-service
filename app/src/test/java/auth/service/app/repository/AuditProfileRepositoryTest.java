package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import auth.service.BaseTest;
import auth.service.app.model.entity.AuditProfileEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class AuditProfileRepositoryTest extends BaseTest {

  @Autowired private AuditProfileRepository auditProfileRepository;

  @Test
  void testFindByProfileId() {
    Page<AuditProfileEntity> auditProfileEntityPage =
        auditProfileRepository.findByProfileId(ID, Pageable.ofSize(10));
    List<AuditProfileEntity> auditProfileEntities = auditProfileEntityPage.toList();
    assertNotNull(auditProfileEntities);
    assertEquals(3, auditProfileEntities.size());
    assertEquals(ID, auditProfileEntities.getFirst().getProfile().getId());
    assertEquals(ID, auditProfileEntities.getLast().getProfile().getId());
  }
}
