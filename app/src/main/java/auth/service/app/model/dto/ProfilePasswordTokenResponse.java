package auth.service.app.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
public class ProfilePasswordTokenResponse {
  @ToString.Exclude
  @JsonProperty("aToken")
  private String aToken;

  @ToString.Exclude
  @JsonProperty("rToken")
  private String rToken;

  private ProfileDto profile;
  private ResponseMetadata responseMetadata;
}
