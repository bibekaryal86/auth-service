package auth.service.app.controller;

import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.token.AuthToken;
import auth.service.app.service.PlatformProfileRoleService;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.EntityDtoConvertUtils;
import auth.service.app.util.PermissionCheck;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/sso/{platformId}")
public class SsoApiController {

  private final PlatformProfileRoleService platformProfileRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final PermissionCheck permissionCheck;

  @GetMapping(value = "/validate/token", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AuthToken> validateToken(@PathVariable final Long platformId) {
    // Token is validated by Spring Security already, now need to return AuthToken
    try {
      final AuthToken authToken = CommonUtils.getAuthentication();
      if (authToken != null
          && authToken.getPlatform() != null
          && Objects.equals(platformId, authToken.getPlatform().getId())) {
        return ResponseEntity.ok(authToken);
      }
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception ex) {
      log.error("Validate Token Error", ex);
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping(value = "/check/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Boolean>> checkPermissions(
      @PathVariable final Long platformId, @RequestBody List<String> permissionsToCheck) {
    try {
      final AuthToken authToken = CommonUtils.getAuthentication();
      if (authToken != null
          && authToken.getPlatform() != null
          && Objects.equals(platformId, authToken.getPlatform().getId())) {
        return ResponseEntity.ok(permissionCheck.checkPermissionDuplicate(permissionsToCheck));
      }
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception ex) {
      log.error("Check Permissions Error", ex);
      return ResponseEntity.internalServerError().build();
    }
  }

  // this mostly mimics ProfileController.readProfilesByPlatformId
  @GetMapping(value = "/ba_profiles/list", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProfileResponse> listSsoProfiles(
      @PathVariable final Long platformId,
      @RequestParam(required = false, defaultValue = "1") final int pageNumber,
      @RequestParam(required = false, defaultValue = "100") final int perPage,
      @RequestParam(required = false, defaultValue = "") final String sortColumn,
      @RequestParam(required = false, defaultValue = "ASC") final Sort.Direction sortDirection) {
    try {
      final RequestMetadata requestMetadata =
          RequestMetadata.builder()
              .isIncludePermissions(Boolean.FALSE)
              .isIncludePlatforms(Boolean.FALSE)
              .isIncludeProfiles(Boolean.FALSE)
              .isIncludeRoles(Boolean.FALSE)
              .isIncludeDeleted(Boolean.FALSE)
              .isIncludeHistory(Boolean.FALSE)
              .pageNumber(pageNumber)
              .perPage((perPage < 10 || perPage > 1000) ? 100 : perPage)
              .historyPage(0)
              .historySize(0)
              .sortColumn(sortColumn.isEmpty() ? "lastName" : sortColumn)
              .sortDirection(sortDirection)
              .build();
      final Page<PlatformProfileRoleEntity> platformProfileRoleEntityPage =
          platformProfileRoleService.readPlatformProfileRolesByPlatformId(
              platformId, requestMetadata);
      final List<PlatformProfileRoleEntity> platformProfileRoleEntities =
          platformProfileRoleEntityPage.toList();
      final List<ProfileEntity> profileEntities =
          platformProfileRoleEntities.stream().map(PlatformProfileRoleEntity::getProfile).toList();
      final ResponseMetadata.ResponsePageInfo responsePageInfo =
          CommonUtils.defaultResponsePageInfo(platformProfileRoleEntityPage);
      return entityDtoConvertUtils.getResponseMultipleProfiles(
          profileEntities,
          requestMetadata.isIncludeRoles(),
          requestMetadata.isIncludePlatforms(),
          Boolean.FALSE,
          responsePageInfo,
          requestMetadata);
    } catch (Exception ex) {
      log.error("List Sso Profiles...", ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }
}
