package auth.service.app.util;

import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class JpaSpecificationUtils {

  public static Specification<PlatformProfileRoleEntity> pprFilters(
      Long platformId, Long roleId, boolean isIncludeUnassigned) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();

      Join<Object, Object> profileJoin = root.join("profile", JoinType.INNER);

      if (platformId != null) {
        predicates.add(builder.equal(root.get("id").get("platformId"), platformId));
      }

      if (roleId != null) {
        predicates.add(builder.equal(root.get("id").get("roleId"), roleId));
      }

      if (!isIncludeUnassigned) {
        predicates.add(builder.isNull(root.get("unassignedDate")));
        predicates.add(builder.isNull(profileJoin.get("deletedDate")));
      }

      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  public static Specification<PlatformRolePermissionEntity> prpFilters(
      Long platformId, Long roleId, boolean isIncludeUnassigned) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();

      Join<Object, Object> permissionJoin = root.join("permission", JoinType.INNER);

      if (platformId != null) {
        predicates.add(builder.equal(root.get("id").get("platformId"), platformId));
      }

      if (roleId != null) {
        predicates.add(builder.equal(root.get("id").get("roleId"), roleId));
      }

      if (!isIncludeUnassigned) {
        predicates.add(builder.isNull(root.get("unassignedDate")));
        predicates.add(builder.isNull(permissionJoin.get("deletedDate")));
      }

      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
