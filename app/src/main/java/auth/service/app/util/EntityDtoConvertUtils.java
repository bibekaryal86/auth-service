package auth.service.app.util;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityDtoConvertUtils {

  public ResponseEntity<ResponseWithMetadata> getResponseErrorResponseMetadata(
      final Exception exception) {
    final HttpStatus httpStatus = CommonUtils.getHttpStatusForErrorResponse(exception);
    return new ResponseEntity<>(
        new ResponseWithMetadata(
            new ResponseMetadata(
                new ResponseMetadata.ResponseStatusInfo(exception.getMessage()),
                ResponseMetadata.emptyResponseCrudInfo(),
                ResponseMetadata.emptyResponsePageInfo())),
        httpStatus);
  }
}
