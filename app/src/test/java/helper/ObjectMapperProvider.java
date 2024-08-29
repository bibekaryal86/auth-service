package helper;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectMapperProvider {

  public static ObjectMapper objectMapper() {
    return new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
  }
}
