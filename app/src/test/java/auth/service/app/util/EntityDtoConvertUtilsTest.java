package auth.service.app.util;

import auth.service.BaseTest;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import helper.TestData;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;

public class EntityDtoConvertUtilsTest extends BaseTest {

  @Autowired private EntityDtoConvertUtils entityDtoConvertUtils;

  private static List<PermissionEntity> permissionEntities;
  private static List<RoleEntity> roleEntities;
  private static List<PlatformEntity> platformEntities;
  private static List<ProfileEntity> profileEntities;
  private static List<PlatformProfileRoleEntity> platformProfileRoleEntities;

  @BeforeAll
  static void setUp() {
    permissionEntities = TestData.getPermissionEntities();
    roleEntities = TestData.getRoleEntities();
    platformEntities = TestData.getPlatformEntities();
    profileEntities = TestData.getProfileEntities();
    platformProfileRoleEntities = TestData.getPlatformProfileRoleEntities();
  }

  // TODO
}
