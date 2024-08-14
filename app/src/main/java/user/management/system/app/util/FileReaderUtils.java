package user.management.system.app.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class FileReaderUtils {
  private final ResourceLoader resourceLoader;

  public FileReaderUtils(final ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public String readFileContents(final String fileName) {
    final Resource resource = resourceLoader.getResource("classpath:" + fileName);

    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
      StringBuilder content = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line).append(System.lineSeparator());
      }
      return content.toString();
    } catch (IOException e) {
        throw new RuntimeException(String.format("Error Reading File Contents: %s", fileName));
    }
  }
}
