package auth.service.app.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
public class ProfilePasswordTokenResponse {
  @ToString.Exclude
  @JsonProperty("accessToken")
  private String accessToken;

  // TODO when building add conditions such that these are ignored in PRODUCTION
  @ToString.Exclude private String refreshToken;
  @ToString.Exclude private String csrfToken;

  private AuthToken authToken;
  private ResponseMetadata responseMetadata;
}
