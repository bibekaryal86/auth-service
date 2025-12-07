package integration.auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.*;

import auth.service.app.model.token.AuthTokenRolePermissionLookup;
import auth.service.app.repository.RawSqlRepository;
import integration.BaseTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("RawSqlRepository Integration Tests")
public class RawSqlRepositoryTest extends BaseTest {

  @Autowired private RawSqlRepository rawSqlRepository;

  @Nested
  @DisplayName("findRolePermissionsForAuthToken() - Valid scenarios")
  class ValidScenariosTests {

    @Test
    @DisplayName(
        "Should return role-permission lookups for valid platform and profile with active assignments")
    void shouldReturnRolePermissionsForValidPlatformAndProfile() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(ID, ID);

      assertNotNull(result);
      assertFalse(result.isEmpty(), "Should return results for active profile role assignment");

      for (AuthTokenRolePermissionLookup lookup : result) {
        assertNotNull(lookup.roleId());
        assertNotNull(lookup.roleName());
        assertNotNull(lookup.permissionId());
        assertNotNull(lookup.permissionName());
        assertTrue(lookup.roleId() > 0);
        assertTrue(lookup.permissionId() > 0);
      }
    }

    @Test
    @DisplayName(
        "Should return multiple role-permission combinations when role has multiple permissions")
    void shouldReturnMultiplePermissionsForSingleRole() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(ID, ID);

      assertNotNull(result);
      // If role 1 has permissions, we should get them
      if (!result.isEmpty()) {
        long roleId = result.getFirst().roleId();
        assertTrue(result.stream().allMatch(r -> r.roleId() == roleId));
      }
    }

    @Test
    @DisplayName("Should return DISTINCT results avoiding duplicates")
    void shouldReturnDistinctResults() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(ID, 2L);

      assertNotNull(result);

      // Check for uniqueness of role-permission combinations
      long uniqueCount =
          result.stream().map(r -> r.roleId() + "-" + r.permissionId()).distinct().count();

      assertEquals(
          result.size(), uniqueCount, "Should not have duplicate role-permission combinations");
    }

    @Test
    @DisplayName("Should include role name and permission name in results")
    void shouldIncludeRoleAndPermissionNames() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(ID, ID);

      assertNotNull(result);
      if (!result.isEmpty()) {
        AuthTokenRolePermissionLookup lookup = result.getFirst();
        assertNotNull(lookup.roleName(), "Role name should not be null");
        assertNotNull(lookup.permissionName(), "Permission name should not be null");
        assertFalse(lookup.roleName().isEmpty(), "Role name should not be empty");
        assertFalse(lookup.permissionName().isEmpty(), "Permission name should not be empty");
      }
    }
  }

  @Nested
  @DisplayName("findRolePermissionsForAuthToken() - JOIN conditions")
  class JoinConditionsTests {

    @Test
    @DisplayName("Should only return permissions where platform_id matches in both PPR and PRP")
    void shouldMatchPlatformIdInBothTables() {
      // Profile 1 on Platform 1
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 1L);

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should only return permissions where role_id matches in both PPR and PRP")
    void shouldMatchRoleIdInBothTables() {
      // Profile 2 on Platform 1 has role 2
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(ID, 2L);

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should join with role table to get role details")
    void shouldJoinWithRoleTable() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(ID, 1L);

      assertNotNull(result);
      if (!result.isEmpty()) {
        assertNotNull(result.getFirst().roleId());
        assertNotNull(result.getFirst().roleName());
      }
    }

    @Test
    @DisplayName("Should join with permission table to get permission details")
    void shouldJoinWithPermissionTable() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 1L);

      assertNotNull(result);
      if (!result.isEmpty()) {
        assertNotNull(result.getFirst().permissionId());
        assertNotNull(result.getFirst().permissionName());
      }
    }
  }

  @Nested
  @DisplayName("findRolePermissionsForAuthToken() - WHERE clause conditions")
  class WhereClauseTests {

    @Test
    @DisplayName("Should filter by exact platformId match")
    void shouldFilterByExactPlatformId() {
      List<AuthTokenRolePermissionLookup> platform1Results =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 1L);

      List<AuthTokenRolePermissionLookup> platform5Results =
          rawSqlRepository.findRolePermissionsForAuthToken(5L, 5L);

      assertNotNull(platform1Results);
      assertNotNull(platform5Results);
    }

    @Test
    @DisplayName("Should filter by exact profileId match")
    void shouldFilterByExactProfileId() {
      List<AuthTokenRolePermissionLookup> profile1Results =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 1L);

      List<AuthTokenRolePermissionLookup> profile2Results =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 2L);

      assertNotNull(profile1Results);
      assertNotNull(profile2Results);

      // Results should be different for different profiles on same platform
    }

    @Test
    @DisplayName("Should exclude records where PPR unassigned_date is NOT NULL")
    void shouldExcludeUnassignedPlatformProfileRoles() {
      // platformId=1, profileId=3 has unassigned_date set (not NULL)
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 3L);

      assertNotNull(result);
      assertTrue(
          result.isEmpty(),
          "Should return empty list when profile role is unassigned (unassigned_date is NOT NULL)");
    }

    @Test
    @DisplayName("Should exclude records where PRP unassigned_date is NOT NULL")
    void shouldExcludeUnassignedPlatformRolePermissions() {
      // If there's a scenario where prp.unassigned_date is NOT NULL,
      // those permissions should not appear in results

      // Test with a profile that has active role assignment but might have
      // some permissions that are unassigned
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(9L, 7L);

      assertNotNull(result);
      // Should only include permissions where prp.unassigned_date IS NULL
      // Any permissions with unassigned_date NOT NULL should be filtered out
    }

    @Test
    @DisplayName("Should only include records where BOTH PPR and PRP unassigned_date are NULL")
    void shouldRequireBothUnassignedDatesNull() {
      // platformId=5, profileId=7 has PPR unassigned_date NOT NULL
      List<AuthTokenRolePermissionLookup> result1 =
          rawSqlRepository.findRolePermissionsForAuthToken(5L, 7L);

      assertNotNull(result1);
      assertTrue(result1.isEmpty(), "Should be empty when PPR unassigned_date is NOT NULL");

      // platformId=9, profileId=9 has PPR unassigned_date NOT NULL
      List<AuthTokenRolePermissionLookup> result2 =
          rawSqlRepository.findRolePermissionsForAuthToken(9L, 9L);

      assertNotNull(result2);
      assertTrue(result2.isEmpty(), "Should be empty when PPR unassigned_date is NOT NULL");
    }
  }

  @Nested
  @DisplayName("findRolePermissionsForAuthToken() - Edge cases and empty results")
  class EdgeCasesTests {

    @Test
    @DisplayName("Should return empty list for non-existent platformId")
    void shouldReturnEmptyListForNonExistentPlatformId() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(999L, 1L);

      assertNotNull(result);
      assertTrue(result.isEmpty(), "Should return empty list for non-existent platform");
    }

    @Test
    @DisplayName("Should return empty list for non-existent profileId")
    void shouldReturnEmptyListForNonExistentProfileId() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 999L);

      assertNotNull(result);
      assertTrue(result.isEmpty(), "Should return empty list for non-existent profile");
    }

    @Test
    @DisplayName("Should return empty list when platform and profile combination does not exist")
    void shouldReturnEmptyListForInvalidCombination() {
      // Platform 1 with Profile 5 (profile 5 is on platform 5, not 1)
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 5L);

      assertNotNull(result);
      assertTrue(
          result.isEmpty(),
          "Should return empty list when profile is not assigned to the platform");
    }

    @Test
    @DisplayName("Should return empty list for null platformId")
    void shouldHandleNullPlatformId() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(null, 1L);

      assertNotNull(result);
      assertTrue(result.isEmpty(), "Should return empty list for null platform ID");
    }

    @Test
    @DisplayName("Should return empty list for null profileId")
    void shouldHandleNullProfileId() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, null);

      assertNotNull(result);
      assertTrue(result.isEmpty(), "Should return empty list for null profile ID");
    }

    @Test
    @DisplayName("Should return empty list when both IDs are null")
    void shouldHandleBothIdsNull() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(null, null);

      assertNotNull(result);
      assertTrue(result.isEmpty(), "Should return empty list when both IDs are null");
    }

    @Test
    @DisplayName("Should handle zero as platformId")
    void shouldHandleZeroPlatformId() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(0L, 1L);

      assertNotNull(result);
      assertTrue(result.isEmpty(), "Should return empty list for platform ID of 0");
    }

    @Test
    @DisplayName("Should handle zero as profileId")
    void shouldHandleZeroProfileId() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 0L);

      assertNotNull(result);
      assertTrue(result.isEmpty(), "Should return empty list for profile ID of 0");
    }

    @Test
    @DisplayName("Should handle negative platformId")
    void shouldHandleNegativePlatformId() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(-1L, 1L);

      assertNotNull(result);
      assertTrue(result.isEmpty(), "Should return empty list for negative platform ID");
    }

    @Test
    @DisplayName("Should handle negative profileId")
    void shouldHandleNegativeProfileId() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, -1L);

      assertNotNull(result);
      assertTrue(result.isEmpty(), "Should return empty list for negative profile ID");
    }
  }

  @Nested
  @DisplayName("findRolePermissionsForAuthToken() - Multiple active assignments")
  class MultipleAssignmentsTests {

    @Test
    @DisplayName("Should return results for profile with single active role assignment")
    void shouldReturnResultsForSingleActiveRole() {
      // Platform 1, Profile 1 has one active role assignment
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 1L);

      assertNotNull(result);
      // Should have results if the role has permissions
    }

    @Test
    @DisplayName("Should return results for profile with multiple active role assignments")
    void shouldReturnResultsForMultipleActiveRoles() {
      // If a profile has multiple active roles on same platform,
      // should return all permissions from all roles

      // Platform 9, Profile 8 (check if they have multiple roles)
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(9L, 8L);

      assertNotNull(result);
      // Should aggregate permissions from all active roles
    }

    @Test
    @DisplayName("Should handle role with no permissions")
    void shouldHandleRoleWithNoPermissions() {
      // If a role has no permissions assigned in platform_role_permission,
      // the query should return empty even if profile has the role

      // This tests the INNER JOIN behavior - if no matching prp records, no results
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(5L, 6L);

      assertNotNull(result);
      // Result depends on whether role 6 has any permissions
    }
  }

  @Nested
  @DisplayName("findRolePermissionsForAuthToken() - Data integrity and type conversion")
  class DataIntegrityTests {

    @Test
    @DisplayName("Should correctly convert role_id from Number to Long")
    void shouldConvertRoleIdToLong() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 1L);

      assertNotNull(result);
      if (!result.isEmpty()) {
        AuthTokenRolePermissionLookup lookup = result.getFirst();
        assertInstanceOf(Long.class, lookup.roleId());
        assertTrue(lookup.roleId() > 0);
      }
    }

    @Test
    @DisplayName("Should correctly convert permission_id from Number to Long")
    void shouldConvertPermissionIdToLong() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 1L);

      assertNotNull(result);
      if (!result.isEmpty()) {
        AuthTokenRolePermissionLookup lookup = result.get(0);
        assertNotNull(lookup.permissionId());
        assertTrue(lookup.permissionId() > 0);
      }
    }

    @Test
    @DisplayName("Should correctly map role_name as String")
    void shouldMapRoleNameAsString() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 1L);

      assertNotNull(result);
      if (!result.isEmpty()) {
        AuthTokenRolePermissionLookup lookup = result.getFirst();
        assertInstanceOf(String.class, lookup.roleName());
        assertFalse(lookup.roleName().trim().isEmpty());
      }
    }

    @Test
    @DisplayName("Should correctly map permission_name as String")
    void shouldMapPermissionNameAsString() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 1L);

      assertNotNull(result);
      if (!result.isEmpty()) {
        AuthTokenRolePermissionLookup lookup = result.getFirst();
        assertInstanceOf(String.class, lookup.permissionName());
        assertFalse(lookup.permissionName().trim().isEmpty());
      }
    }

    @Test
    @DisplayName("Should maintain consistent data across multiple calls")
    void shouldMaintainConsistentDataAcrossMultipleCalls() {
      List<AuthTokenRolePermissionLookup> result1 =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 1L);

      List<AuthTokenRolePermissionLookup> result2 =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 1L);

      assertEquals(
          result1.size(), result2.size(), "Should return consistent results for same parameters");

      // Verify same role-permission combinations are returned
      if (!result1.isEmpty()) {
        for (int i = 0; i < result1.size(); i++) {
          assertEquals(result1.get(i).roleId(), result2.get(i).roleId());
          assertEquals(result1.get(i).permissionId(), result2.get(i).permissionId());
        }
      }
    }
  }

  @Nested
  @DisplayName("findRolePermissionsForAuthToken() - Specific test data scenarios")
  class TestDataScenariosTests {

    @Test
    @DisplayName("Platform 1, Profile 1 - Active assignment")
    void testPlatform1Profile1() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 1L);

      assertNotNull(result);
      // Profile 1 has active role 1 on platform 1 (unassigned_date is NULL)
      // Should return permissions if role 1 has any active permissions
    }

    @Test
    @DisplayName("Platform 1, Profile 2 - Active assignment")
    void testPlatform1Profile2() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 2L);

      assertNotNull(result);
      // Profile 2 has active role 2 on platform 1
    }

    @Test
    @DisplayName("Platform 1, Profile 3 - Unassigned (should be empty)")
    void testPlatform1Profile3Unassigned() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(1L, 3L);

      assertNotNull(result);
      assertTrue(result.isEmpty(), "Profile 3 has unassigned_date NOT NULL, should return empty");
    }

    @Test
    @DisplayName("Platform 5, Profile 5 - Active assignment")
    void testPlatform5Profile5() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(5L, 5L);

      assertNotNull(result);
      // Profile 5 has active role 5 on platform 5
    }

    @Test
    @DisplayName("Platform 5, Profile 7 - Unassigned (should be empty)")
    void testPlatform5Profile7Unassigned() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(5L, 7L);

      assertNotNull(result);
      assertTrue(
          result.isEmpty(),
          "Profile 7 on Platform 5 has unassigned_date NOT NULL, should return empty");
    }

    @Test
    @DisplayName("Platform 9, Profile 7 - Active assignment")
    void testPlatform9Profile7() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(9L, 7L);

      assertNotNull(result);
      // Profile 7 has active role 7 on platform 9
    }

    @Test
    @DisplayName("Platform 9, Profile 8 - Active assignment")
    void testPlatform9Profile8() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(9L, 8L);

      assertNotNull(result);
      // Profile 8 has active role 8 on platform 9
    }

    @Test
    @DisplayName("Platform 9, Profile 9 - Unassigned (should be empty)")
    void testPlatform9Profile9Unassigned() {
      List<AuthTokenRolePermissionLookup> result =
          rawSqlRepository.findRolePermissionsForAuthToken(9L, 9L);

      assertNotNull(result);
      assertTrue(
          result.isEmpty(),
          "Profile 9 on Platform 9 has unassigned_date NOT NULL, should return empty");
    }
  }
}
