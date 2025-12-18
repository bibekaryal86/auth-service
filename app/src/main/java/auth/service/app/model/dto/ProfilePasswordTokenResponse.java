package auth.service.app.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class ProfilePasswordTokenResponse {
  private String accessToken;

  @JsonIgnore private String refreshToken;

  @JsonIgnore private String csrfToken;

  private AuthToken authToken;
  private ResponseMetadata responseMetadata;

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  @JsonIgnore
  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  @JsonIgnore
  public String getCsrfToken() {
    return csrfToken;
  }

  public void setCsrfToken(String csrfToken) {
    this.csrfToken = csrfToken;
  }

  public AuthToken getAuthToken() {
    return authToken;
  }

  public void setAuthToken(AuthToken authToken) {
    this.authToken = authToken;
  }

  public ResponseMetadata getResponseMetadata() {
    return responseMetadata;
  }

  public void setResponseMetadata(ResponseMetadata responseMetadata) {
    this.responseMetadata = responseMetadata;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ProfilePasswordTokenResponse that)) return false;
    return Objects.equals(accessToken, that.accessToken)
        && Objects.equals(refreshToken, that.refreshToken)
        && Objects.equals(csrfToken, that.csrfToken)
        && Objects.equals(authToken, that.authToken)
        && Objects.equals(responseMetadata, that.responseMetadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessToken, refreshToken, csrfToken, authToken, responseMetadata);
  }

  @Override
  public String toString() {
    return "ProfilePasswordTokenResponse{" + "responseMetadata=" + responseMetadata + '}';
  }
}
