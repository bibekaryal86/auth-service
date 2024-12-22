package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
public class ProfilePasswordTokenResponse {
  @ToString.Exclude private String aToken;
  @ToString.Exclude private String rToken;
  private ProfileDto profile;
  private ResponseMetadata responseMetadata;
}
