package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JpaSpecificationUtils Unit Tests")
public class JpaSpecificationUtilsTest {

  @Mock private Root<PlatformProfileRoleEntity> pprRoot;
  @Mock private Root<PlatformRolePermissionEntity> prpRoot;
  @Mock private CriteriaQuery<?> query;
  @Mock private CriteriaBuilder builder;
  @Mock private Path<Object> idPath;
  @Mock private Path<Object> platformIdPath;
  @Mock private Path<Object> roleIdPath;
  @Mock private Path<Object> unassignedDatePath;
  @Mock private Join<Object, Object> profileJoin;
  @Mock private Join<Object, Object> permissionJoin;
  @Mock private Path<Object> deletedDatePath;
  @Mock private Predicate platformPredicate;
  @Mock private Predicate rolePredicate;
  @Mock private Predicate unassignedDatePredicate;
  @Mock private Predicate deletedDatePredicate;
  @Mock private Predicate andPredicate;

  private static final Long PLATFORM_ID = 1L;
  private static final Long ROLE_ID = 100L;

  @Nested
  @DisplayName("pprFilters() tests")
  class PprFiltersTests {

    @BeforeEach
    void setUp() {
      when(pprRoot.join("profile", JoinType.INNER)).thenReturn(profileJoin);
      when(pprRoot.get("id")).thenReturn(idPath);
      when(idPath.get("platformId")).thenReturn(platformIdPath);
      when(idPath.get("roleId")).thenReturn(roleIdPath);
      when(pprRoot.get("unassignedDate")).thenReturn(unassignedDatePath);
      when(profileJoin.get("deletedDate")).thenReturn(deletedDatePath);
      when(builder.and(any(Predicate[].class))).thenReturn(andPredicate);
    }

    @Test
    @DisplayName("Should create specification with all filters when platformId and roleId provided")
    void shouldCreateSpecificationWithAllFilters() {
      when(builder.equal(platformIdPath, PLATFORM_ID)).thenReturn(platformPredicate);
      when(builder.equal(roleIdPath, ROLE_ID)).thenReturn(rolePredicate);
      when(builder.isNull(unassignedDatePath)).thenReturn(unassignedDatePredicate);
      when(builder.isNull(deletedDatePath)).thenReturn(deletedDatePredicate);

      Specification<PlatformProfileRoleEntity> spec =
          JpaSpecificationUtils.pprFilters(PLATFORM_ID, ROLE_ID, false);

      Predicate result = spec.toPredicate(pprRoot, query, builder);

      assertNotNull(result);
      verify(pprRoot).join("profile", JoinType.INNER);
      verify(builder).equal(platformIdPath, PLATFORM_ID);
      verify(builder).equal(roleIdPath, ROLE_ID);
      verify(builder).isNull(unassignedDatePath);
      verify(builder).isNull(deletedDatePath);
      verify(builder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should create specification with only platformId filter")
    void shouldCreateSpecificationWithOnlyPlatformId() {
      when(builder.equal(platformIdPath, PLATFORM_ID)).thenReturn(platformPredicate);
      when(builder.isNull(unassignedDatePath)).thenReturn(unassignedDatePredicate);
      when(builder.isNull(deletedDatePath)).thenReturn(deletedDatePredicate);

      Specification<PlatformProfileRoleEntity> spec =
          JpaSpecificationUtils.pprFilters(PLATFORM_ID, null, false);

      Predicate result = spec.toPredicate(pprRoot, query, builder);

      assertNotNull(result);
      verify(builder).equal(platformIdPath, PLATFORM_ID);
      verify(builder, never()).equal(eq(roleIdPath), any());
      verify(builder).isNull(unassignedDatePath);
      verify(builder).isNull(deletedDatePath);
    }

    @Test
    @DisplayName("Should create specification with only roleId filter")
    void shouldCreateSpecificationWithOnlyRoleId() {
      when(builder.equal(roleIdPath, ROLE_ID)).thenReturn(rolePredicate);
      when(builder.isNull(unassignedDatePath)).thenReturn(unassignedDatePredicate);
      when(builder.isNull(deletedDatePath)).thenReturn(deletedDatePredicate);

      Specification<PlatformProfileRoleEntity> spec =
          JpaSpecificationUtils.pprFilters(null, ROLE_ID, false);

      Predicate result = spec.toPredicate(pprRoot, query, builder);

      assertNotNull(result);
      verify(builder, never()).equal(eq(platformIdPath), any());
      verify(builder).equal(roleIdPath, ROLE_ID);
      verify(builder).isNull(unassignedDatePath);
      verify(builder).isNull(deletedDatePath);
    }

    @Test
    @DisplayName("Should include unassigned when isIncludeUnassigned is true")
    void shouldIncludeUnassignedWhenFlagIsTrue() {
      when(builder.equal(platformIdPath, PLATFORM_ID)).thenReturn(platformPredicate);

      Specification<PlatformProfileRoleEntity> spec =
          JpaSpecificationUtils.pprFilters(PLATFORM_ID, null, true);

      Predicate result = spec.toPredicate(pprRoot, query, builder);

      assertNotNull(result);
      verify(builder).equal(platformIdPath, PLATFORM_ID);
      verify(builder, never()).isNull(any(Path.class));
    }

    @Test
    @DisplayName("Should exclude unassigned when isIncludeUnassigned is false")
    void shouldExcludeUnassignedWhenFlagIsFalse() {
      when(builder.isNull(unassignedDatePath)).thenReturn(unassignedDatePredicate);
      when(builder.isNull(deletedDatePath)).thenReturn(deletedDatePredicate);

      Specification<PlatformProfileRoleEntity> spec =
          JpaSpecificationUtils.pprFilters(null, null, false);

      Predicate result = spec.toPredicate(pprRoot, query, builder);

      assertNotNull(result);
      verify(builder).isNull(unassignedDatePath);
      verify(builder).isNull(deletedDatePath);
    }

    @Test
    @DisplayName("Should create specification with no filters when all parameters are null/true")
    void shouldCreateSpecificationWithNoFilters() {
      Specification<PlatformProfileRoleEntity> spec =
          JpaSpecificationUtils.pprFilters(null, null, true);

      Predicate result = spec.toPredicate(pprRoot, query, builder);

      assertNotNull(result);
      verify(builder, never()).equal(any(Path.class), any());
      verify(builder, never()).isNull(any(Path.class));
      verify(builder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should create INNER join with profile")
    void shouldCreateInnerJoinWithProfile() {
      Specification<PlatformProfileRoleEntity> spec =
          JpaSpecificationUtils.pprFilters(null, null, false);

      spec.toPredicate(pprRoot, query, builder);

      verify(pprRoot).join("profile", JoinType.INNER);
    }

    @Test
    @DisplayName("Should handle platformId of zero")
    void shouldHandlePlatformIdOfZero() {
      Long zeroPlatformId = 0L;
      when(builder.equal(platformIdPath, zeroPlatformId)).thenReturn(platformPredicate);

      Specification<PlatformProfileRoleEntity> spec =
          JpaSpecificationUtils.pprFilters(zeroPlatformId, null, true);

      Predicate result = spec.toPredicate(pprRoot, query, builder);

      assertNotNull(result);
      verify(builder).equal(platformIdPath, zeroPlatformId);
    }

    @Test
    @DisplayName("Should handle roleId of zero")
    void shouldHandleRoleIdOfZero() {
      Long zeroRoleId = 0L;
      when(builder.equal(roleIdPath, zeroRoleId)).thenReturn(rolePredicate);

      Specification<PlatformProfileRoleEntity> spec =
          JpaSpecificationUtils.pprFilters(null, zeroRoleId, true);

      Predicate result = spec.toPredicate(pprRoot, query, builder);

      assertNotNull(result);
      verify(builder).equal(roleIdPath, zeroRoleId);
    }

    @Test
    @DisplayName("Should build predicates array correctly")
    void shouldBuildPredicatesArrayCorrectly() {
      // Arrange
      when(builder.equal(platformIdPath, PLATFORM_ID)).thenReturn(platformPredicate);
      when(builder.equal(roleIdPath, ROLE_ID)).thenReturn(rolePredicate);
      when(builder.isNull(unassignedDatePath)).thenReturn(unassignedDatePredicate);
      when(builder.isNull(deletedDatePath)).thenReturn(deletedDatePredicate);

      Specification<PlatformProfileRoleEntity> spec =
          JpaSpecificationUtils.pprFilters(PLATFORM_ID, ROLE_ID, false);

      // Act
      spec.toPredicate(pprRoot, query, builder);

      // Assert: Capture predicates passed to builder.and(...)
      ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);

      verify(builder).and(captor.capture());

      Predicate[] predicates = captor.getValue();

      // Basic count assertion
      assertEquals(4, predicates.length, "Expected 4 predicates");

      // Optional: Validate ordering if your implementation guarantees order
      assertArrayEquals(
          new Predicate[] {
            platformPredicate, rolePredicate, unassignedDatePredicate, deletedDatePredicate
          },
          predicates,
          "Predicates should be built in expected order");
    }
  }

  @Nested
  @DisplayName("prpFilters() tests")
  class PrpFiltersTests {

    @BeforeEach
    void setUp() {
      when(prpRoot.join("permission", JoinType.INNER)).thenReturn(permissionJoin);
      when(prpRoot.get("id")).thenReturn(idPath);
      when(idPath.get("platformId")).thenReturn(platformIdPath);
      when(idPath.get("roleId")).thenReturn(roleIdPath);
      when(prpRoot.get("unassignedDate")).thenReturn(unassignedDatePath);
      when(permissionJoin.get("deletedDate")).thenReturn(deletedDatePath);
      when(builder.and(any(Predicate[].class))).thenReturn(andPredicate);
    }

    @Test
    @DisplayName("Should create specification with all filters when platformId and roleId provided")
    void shouldCreateSpecificationWithAllFilters() {
      when(builder.equal(platformIdPath, PLATFORM_ID)).thenReturn(platformPredicate);
      when(builder.equal(roleIdPath, ROLE_ID)).thenReturn(rolePredicate);
      when(builder.isNull(unassignedDatePath)).thenReturn(unassignedDatePredicate);
      when(builder.isNull(deletedDatePath)).thenReturn(deletedDatePredicate);

      Specification<PlatformRolePermissionEntity> spec =
          JpaSpecificationUtils.prpFilters(PLATFORM_ID, ROLE_ID, false);

      Predicate result = spec.toPredicate(prpRoot, query, builder);

      assertNotNull(result);
      verify(prpRoot).join("permission", JoinType.INNER);
      verify(builder).equal(platformIdPath, PLATFORM_ID);
      verify(builder).equal(roleIdPath, ROLE_ID);
      verify(builder).isNull(unassignedDatePath);
      verify(builder).isNull(deletedDatePath);
      verify(builder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should create specification with only platformId filter")
    void shouldCreateSpecificationWithOnlyPlatformId() {
      when(builder.equal(platformIdPath, PLATFORM_ID)).thenReturn(platformPredicate);
      when(builder.isNull(unassignedDatePath)).thenReturn(unassignedDatePredicate);
      when(builder.isNull(deletedDatePath)).thenReturn(deletedDatePredicate);

      Specification<PlatformRolePermissionEntity> spec =
          JpaSpecificationUtils.prpFilters(PLATFORM_ID, null, false);

      Predicate result = spec.toPredicate(prpRoot, query, builder);

      assertNotNull(result);
      verify(builder).equal(platformIdPath, PLATFORM_ID);
      verify(builder, never()).equal(eq(roleIdPath), any());
      verify(builder).isNull(unassignedDatePath);
      verify(builder).isNull(deletedDatePath);
    }

    @Test
    @DisplayName("Should create specification with only roleId filter")
    void shouldCreateSpecificationWithOnlyRoleId() {
      when(builder.equal(roleIdPath, ROLE_ID)).thenReturn(rolePredicate);
      when(builder.isNull(unassignedDatePath)).thenReturn(unassignedDatePredicate);
      when(builder.isNull(deletedDatePath)).thenReturn(deletedDatePredicate);

      Specification<PlatformRolePermissionEntity> spec =
          JpaSpecificationUtils.prpFilters(null, ROLE_ID, false);

      Predicate result = spec.toPredicate(prpRoot, query, builder);

      assertNotNull(result);
      verify(builder, never()).equal(eq(platformIdPath), any());
      verify(builder).equal(roleIdPath, ROLE_ID);
      verify(builder).isNull(unassignedDatePath);
      verify(builder).isNull(deletedDatePath);
    }

    @Test
    @DisplayName("Should include unassigned when isIncludeUnassigned is true")
    void shouldIncludeUnassignedWhenFlagIsTrue() {
      when(builder.equal(platformIdPath, PLATFORM_ID)).thenReturn(platformPredicate);

      Specification<PlatformRolePermissionEntity> spec =
          JpaSpecificationUtils.prpFilters(PLATFORM_ID, null, true);

      Predicate result = spec.toPredicate(prpRoot, query, builder);

      assertNotNull(result);
      verify(builder).equal(platformIdPath, PLATFORM_ID);
      verify(builder, never()).isNull(any(Path.class));
    }

    @Test
    @DisplayName("Should exclude unassigned when isIncludeUnassigned is false")
    void shouldExcludeUnassignedWhenFlagIsFalse() {
      when(builder.isNull(unassignedDatePath)).thenReturn(unassignedDatePredicate);
      when(builder.isNull(deletedDatePath)).thenReturn(deletedDatePredicate);

      Specification<PlatformRolePermissionEntity> spec =
          JpaSpecificationUtils.prpFilters(null, null, false);

      Predicate result = spec.toPredicate(prpRoot, query, builder);

      assertNotNull(result);
      verify(builder).isNull(unassignedDatePath);
      verify(builder).isNull(deletedDatePath);
    }

    @Test
    @DisplayName("Should create specification with no filters when all parameters are null/true")
    void shouldCreateSpecificationWithNoFilters() {
      Specification<PlatformRolePermissionEntity> spec =
          JpaSpecificationUtils.prpFilters(null, null, true);

      Predicate result = spec.toPredicate(prpRoot, query, builder);

      assertNotNull(result);
      verify(builder, never()).equal(any(Path.class), any());
      verify(builder, never()).isNull(any(Path.class));
      verify(builder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should create INNER join with permission")
    void shouldCreateInnerJoinWithPermission() {
      Specification<PlatformRolePermissionEntity> spec =
          JpaSpecificationUtils.prpFilters(null, null, false);

      spec.toPredicate(prpRoot, query, builder);

      verify(prpRoot).join("permission", JoinType.INNER);
    }

    @Test
    @DisplayName("Should handle platformId of zero")
    void shouldHandlePlatformIdOfZero() {
      Long zeroPlatformId = 0L;
      when(builder.equal(platformIdPath, zeroPlatformId)).thenReturn(platformPredicate);

      Specification<PlatformRolePermissionEntity> spec =
          JpaSpecificationUtils.prpFilters(zeroPlatformId, null, true);

      Predicate result = spec.toPredicate(prpRoot, query, builder);

      assertNotNull(result);
      verify(builder).equal(platformIdPath, zeroPlatformId);
    }

    @Test
    @DisplayName("Should handle roleId of zero")
    void shouldHandleRoleIdOfZero() {
      Long zeroRoleId = 0L;
      when(builder.equal(roleIdPath, zeroRoleId)).thenReturn(rolePredicate);

      Specification<PlatformRolePermissionEntity> spec =
          JpaSpecificationUtils.prpFilters(null, zeroRoleId, true);

      Predicate result = spec.toPredicate(prpRoot, query, builder);

      assertNotNull(result);
      verify(builder).equal(roleIdPath, zeroRoleId);
    }

    @Test
    @DisplayName("Should build predicates array correctly")
    void shouldBuildPredicatesArrayCorrectly() {
      when(builder.equal(platformIdPath, PLATFORM_ID)).thenReturn(platformPredicate);
      when(builder.equal(roleIdPath, ROLE_ID)).thenReturn(rolePredicate);
      when(builder.isNull(unassignedDatePath)).thenReturn(unassignedDatePredicate);
      when(builder.isNull(deletedDatePath)).thenReturn(deletedDatePredicate);

      Specification<PlatformRolePermissionEntity> spec =
          JpaSpecificationUtils.prpFilters(PLATFORM_ID, ROLE_ID, false);

      spec.toPredicate(prpRoot, query, builder);

      ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);

      verify(builder).and(captor.capture());

      Predicate[] predicates = captor.getValue();

      // Assert
      assertEquals(4, predicates.length, "Expected 4 predicates");
      assertArrayEquals(
          new Predicate[] {
            platformPredicate, rolePredicate, unassignedDatePredicate, deletedDatePredicate
          },
          predicates,
          "Predicates should match expected order");
    }
  }

  @Nested
  @DisplayName("Comparison tests between pprFilters and prpFilters")
  class ComparisonTests {

    @Test
    @DisplayName("Should create different join types for ppr vs prp")
    void shouldCreateDifferentJoinTypes() {
      // Setup only what we need for this specific test
      when(pprRoot.join("profile", JoinType.INNER)).thenReturn(profileJoin);
      when(prpRoot.join("permission", JoinType.INNER)).thenReturn(permissionJoin);
      when(pprRoot.get("id")).thenReturn(idPath);
      when(prpRoot.get("id")).thenReturn(idPath);
      when(pprRoot.get("unassignedDate")).thenReturn(unassignedDatePath);
      when(prpRoot.get("unassignedDate")).thenReturn(unassignedDatePath);
      when(profileJoin.get("deletedDate")).thenReturn(deletedDatePath);
      when(permissionJoin.get("deletedDate")).thenReturn(deletedDatePath);
      when(builder.and(any(Predicate[].class))).thenReturn(andPredicate);

      Specification<PlatformProfileRoleEntity> pprSpec =
          JpaSpecificationUtils.pprFilters(null, null, true);
      Specification<PlatformRolePermissionEntity> prpSpec =
          JpaSpecificationUtils.prpFilters(null, null, true);

      pprSpec.toPredicate(pprRoot, query, builder);
      prpSpec.toPredicate(prpRoot, query, builder);

      verify(pprRoot).join("profile", JoinType.INNER);
      verify(prpRoot).join("permission", JoinType.INNER);
    }

    @Test
    @DisplayName("Should apply same filter logic to both specifications")
    void shouldApplySameFilterLogicToBothSpecs() {
      // Setup for pprFilters
      when(pprRoot.join("profile", JoinType.INNER)).thenReturn(profileJoin);
      when(pprRoot.get("id")).thenReturn(idPath);
      when(pprRoot.get("unassignedDate")).thenReturn(unassignedDatePath);
      when(profileJoin.get("deletedDate")).thenReturn(deletedDatePath);

      // Setup for prpFilters
      when(prpRoot.join("permission", JoinType.INNER)).thenReturn(permissionJoin);
      when(prpRoot.get("id")).thenReturn(idPath);
      when(prpRoot.get("unassignedDate")).thenReturn(unassignedDatePath);
      when(permissionJoin.get("deletedDate")).thenReturn(deletedDatePath);

      when(idPath.get("platformId")).thenReturn(platformIdPath);
      when(idPath.get("roleId")).thenReturn(roleIdPath);
      when(builder.equal(platformIdPath, PLATFORM_ID)).thenReturn(platformPredicate);
      when(builder.equal(roleIdPath, ROLE_ID)).thenReturn(rolePredicate);
      when(builder.isNull(unassignedDatePath)).thenReturn(unassignedDatePredicate);
      when(builder.isNull(deletedDatePath)).thenReturn(deletedDatePredicate);
      when(builder.and(any(Predicate[].class))).thenReturn(andPredicate);

      Specification<PlatformProfileRoleEntity> pprSpec =
          JpaSpecificationUtils.pprFilters(PLATFORM_ID, ROLE_ID, false);
      Specification<PlatformRolePermissionEntity> prpSpec =
          JpaSpecificationUtils.prpFilters(PLATFORM_ID, ROLE_ID, false);

      pprSpec.toPredicate(pprRoot, query, builder);
      prpSpec.toPredicate(prpRoot, query, builder);

      // Both should call equal twice (platformId and roleId)
      verify(builder, times(2)).equal(platformIdPath, PLATFORM_ID);
      verify(builder, times(2)).equal(roleIdPath, ROLE_ID);
      // Both should call isNull twice (unassignedDate and deletedDate)
      verify(builder, times(2)).isNull(unassignedDatePath);
      verify(builder, times(2)).isNull(deletedDatePath);
    }
  }
}
