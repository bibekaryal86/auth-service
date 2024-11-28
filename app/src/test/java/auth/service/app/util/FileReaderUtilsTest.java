package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FileReaderUtilsTest extends BaseTest {

  @Autowired private FileReaderUtils fileReaderUtils;

  @Test
  public void testReadFileContents_Success() {
    String fileName = "fixtures/file-reader-utils-test.txt";
    String expectedContent = "read this line\nand this line\n"; // Match the contents of the file

    String result = fileReaderUtils.readFileContents(fileName);
    assertEquals(expectedContent, result);
  }

  @Test
  void testReadFileContents_FileDoesNotExist() {
    String fileName = "non-existent-file.txt";
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              fileReaderUtils.readFileContents(fileName);
            });
    assertEquals(
        String.format("Error Reading File Contents: %s", fileName), exception.getMessage());
  }
}
