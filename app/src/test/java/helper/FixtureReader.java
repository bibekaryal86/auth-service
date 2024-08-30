package helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FixtureReader {

  public static String readFixture(final String fileWhichExistsInResources) {
    // ../ exists because FixtureReader lives in helper package
    URL url = FixtureReader.class.getResource("../fixtures/" + fileWhichExistsInResources);

    try {
      if (url == null) {
        throw new FileNotFoundException(fileWhichExistsInResources);
      }

      Path path = Paths.get(url.toURI());
      return String.join("\n", Files.readAllLines(path));
    } catch (IOException | URISyntaxException ex) {
      return null;
    }
  }
}
