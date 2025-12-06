package unit.auth.service.app.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import auth.service.app.util.FileReaderUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("FileReaderUtils Unit Tests")
class FileReaderUtilsTest {

  @Mock private ResourceLoader resourceLoader;
  @Mock private Resource resource;

  @InjectMocks private FileReaderUtils fileReaderUtils;

  private static final String TEST_FILE_NAME = "test-file.txt";
  private static final String CLASSPATH_PREFIX = "classpath:";

  @Nested
  @DisplayName("readFileContents() - Success scenarios")
  class SuccessScenarios {

    @Test
    @DisplayName("Should read single line file successfully")
    void shouldReadSingleLineFile() throws IOException {
      String fileContent = "Single line content";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(TEST_FILE_NAME);

      assertNotNull(result);
      assertTrue(result.startsWith("Single line content"));
      verify(resourceLoader).getResource(CLASSPATH_PREFIX + TEST_FILE_NAME);
      verify(resource).getInputStream();
    }

    @Test
    @DisplayName("Should read multi-line file successfully")
    void shouldReadMultiLineFile() throws IOException {
      String fileContent = "Line 1\nLine 2\nLine 3";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(TEST_FILE_NAME);

      assertNotNull(result);
      assertTrue(result.contains("Line 1"));
      assertTrue(result.contains("Line 2"));
      assertTrue(result.contains("Line 3"));
    }

    @Test
    @DisplayName("Should read empty file successfully")
    void shouldReadEmptyFile() throws IOException {
      String fileContent = "";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(TEST_FILE_NAME);

      assertNotNull(result);
      assertTrue(result.isEmpty() || result.equals(System.lineSeparator()));
    }

    @Test
    @DisplayName("Should read file with special characters")
    void shouldReadFileWithSpecialCharacters() throws IOException {
      String fileContent = "Special chars: !@#$%^&*()_+-={}[]|\\:;\"'<>,.?/~`";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(TEST_FILE_NAME);

      assertNotNull(result);
      assertTrue(result.contains("Special chars"));
      assertTrue(result.contains("!@#$%^&*()"));
    }

    @Test
    @DisplayName("Should read file with UTF-8 characters")
    void shouldReadFileWithUtf8Characters() throws IOException {
      String fileContent = "UTF-8: café, naïve, 日本語, émoji: 😀";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(TEST_FILE_NAME);

      assertNotNull(result);
      assertTrue(result.contains("café"));
      assertTrue(result.contains("naïve"));
      assertTrue(result.contains("日本語"));
    }

    @Test
    @DisplayName("Should read file with tabs and spaces")
    void shouldReadFileWithTabsAndSpaces() throws IOException {
      String fileContent = "Line with\ttabs\nLine with    spaces";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(TEST_FILE_NAME);

      assertNotNull(result);
      assertTrue(result.contains("tabs"));
      assertTrue(result.contains("spaces"));
    }

    @Test
    @DisplayName("Should append system line separator after each line")
    void shouldAppendSystemLineSeparator() throws IOException {
      String fileContent = "Line 1\nLine 2";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(TEST_FILE_NAME);

      assertNotNull(result);
      // Verify that system line separator is used
      assertTrue(result.contains(System.lineSeparator()));
    }

    @Test
    @DisplayName("Should read file with only whitespace lines")
    void shouldReadFileWithWhitespaceLines() throws IOException {
      String fileContent = "Line 1\n   \nLine 3";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(TEST_FILE_NAME);

      assertNotNull(result);
      assertTrue(result.contains("Line 1"));
      assertTrue(result.contains("Line 3"));
    }

    @Test
    @DisplayName("Should properly handle large file content")
    void shouldHandleLargeFileContent() throws IOException {
      StringBuilder largeContent = new StringBuilder();
      for (int i = 0; i < 1000; i++) {
        largeContent.append("Line ").append(i).append("\n");
      }
      InputStream inputStream =
          new ByteArrayInputStream(largeContent.toString().getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(TEST_FILE_NAME);

      assertNotNull(result);
      assertTrue(result.contains("Line 0"));
      assertTrue(result.contains("Line 999"));
    }
  }

  @Nested
  @DisplayName("readFileContents() - File path handling")
  class FilePathHandling {

    @Test
    @DisplayName("Should prepend 'classpath:' to file name")
    void shouldPrependClasspathToFileName() throws IOException {
      String fileContent = "Content";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      fileReaderUtils.readFileContents(TEST_FILE_NAME);

      verify(resourceLoader).getResource(CLASSPATH_PREFIX + TEST_FILE_NAME);
    }

    @Test
    @DisplayName("Should handle file name with path separators")
    void shouldHandleFileNameWithPathSeparators() throws IOException {
      String fileName = "templates/email/welcome.html";
      String fileContent = "HTML content";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + fileName)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(fileName);

      assertNotNull(result);
      verify(resourceLoader).getResource(CLASSPATH_PREFIX + fileName);
    }

    @Test
    @DisplayName("Should handle file name with different extensions")
    void shouldHandleFileNameWithDifferentExtensions() throws IOException {
      String[] fileNames = {"file.txt", "file.html", "file.json", "file.xml", "file.properties"};

      for (String fileName : fileNames) {
        String fileContent = "Content for " + fileName;
        InputStream inputStream =
            new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

        when(resourceLoader.getResource(CLASSPATH_PREFIX + fileName)).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(inputStream);

        String result = fileReaderUtils.readFileContents(fileName);

        assertNotNull(result);
        verify(resourceLoader).getResource(CLASSPATH_PREFIX + fileName);
      }
    }

    @Test
    @DisplayName("Should handle file name without extension")
    void shouldHandleFileNameWithoutExtension() throws IOException {
      String fileName = "README";
      String fileContent = "README content";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + fileName)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(fileName);

      assertNotNull(result);
      verify(resourceLoader).getResource(CLASSPATH_PREFIX + fileName);
    }
  }

  @Nested
  @DisplayName("readFileContents() - Error handling")
  class ErrorHandling {

    @Test
    @DisplayName("Should throw RuntimeException when resource input stream throws IOException")
    void shouldThrowRuntimeExceptionWhenInputStreamThrowsIOException() throws IOException {
      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenThrow(new IOException("File not found"));

      RuntimeException exception =
          assertThrows(
              RuntimeException.class, () -> fileReaderUtils.readFileContents(TEST_FILE_NAME));

      assertEquals("Error Reading File Contents: " + TEST_FILE_NAME, exception.getMessage());
      verify(resourceLoader).getResource(CLASSPATH_PREFIX + TEST_FILE_NAME);
    }

    @Test
    @DisplayName("Should throw RuntimeException with correct error message")
    void shouldThrowRuntimeExceptionWithCorrectMessage() throws IOException {
      String fileName = "missing-file.txt";
      when(resourceLoader.getResource(CLASSPATH_PREFIX + fileName)).thenReturn(resource);
      when(resource.getInputStream()).thenThrow(new IOException("Access denied"));

      RuntimeException exception =
          assertThrows(RuntimeException.class, () -> fileReaderUtils.readFileContents(fileName));

      assertTrue(exception.getMessage().contains("Error Reading File Contents"));
      assertTrue(exception.getMessage().contains(fileName));
    }

    @Test
    @DisplayName("Should handle IOException during stream reading")
    void shouldHandleIOExceptionDuringStreamReading() throws IOException {
      InputStream mockInputStream = mock(InputStream.class);
      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(mockInputStream);
      when(mockInputStream.read(any())).thenThrow(new IOException("Read error"));

      RuntimeException exception =
          assertThrows(
              RuntimeException.class, () -> fileReaderUtils.readFileContents(TEST_FILE_NAME));
    }

    @Test
    @DisplayName("Should throw RuntimeException for null file name")
    void shouldHandleNullFileName() throws IOException {
      when(resourceLoader.getResource(CLASSPATH_PREFIX + null)).thenReturn(resource);
      when(resource.getInputStream()).thenThrow(new IOException("Invalid file"));

      RuntimeException exception =
          assertThrows(RuntimeException.class, () -> fileReaderUtils.readFileContents(null));

      assertTrue(exception.getMessage().contains("Error Reading File Contents"));
    }

    @Test
    @DisplayName("Should throw RuntimeException for empty file name")
    void shouldHandleEmptyFileName() throws IOException {
      String fileName = "";
      when(resourceLoader.getResource(CLASSPATH_PREFIX + fileName)).thenReturn(resource);
      when(resource.getInputStream()).thenThrow(new IOException("Invalid file"));

      RuntimeException exception =
          assertThrows(RuntimeException.class, () -> fileReaderUtils.readFileContents(fileName));

      assertTrue(exception.getMessage().contains("Error Reading File Contents"));
    }
  }

  @Nested
  @DisplayName("readFileContents() - Resource management")
  class ResourceManagement {

    @Test
    @DisplayName("Should close input stream after successful read")
    void shouldCloseInputStreamAfterSuccessfulRead() throws IOException {
      String fileContent = "Content";
      InputStream inputStream =
          spy(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      fileReaderUtils.readFileContents(TEST_FILE_NAME);

      verify(inputStream).close();
    }

    @Test
    @DisplayName("Should close input stream even when IOException occurs")
    void shouldCloseInputStreamOnIOException() throws IOException {
      InputStream mockInputStream = mock(InputStream.class);
      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(mockInputStream);
      when(mockInputStream.read(any())).thenThrow(new IOException("Read error"));

      try {
        fileReaderUtils.readFileContents(TEST_FILE_NAME);
      } catch (RuntimeException e) {
        // Expected exception
      }

      verify(mockInputStream).close();
    }

    @Test
    @DisplayName("Should use try-with-resources for proper resource management")
    void shouldUseTryWithResourcesPattern() throws IOException {
      String fileContent = "Content";
      InputStream inputStream =
          spy(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      fileReaderUtils.readFileContents(TEST_FILE_NAME);

      // Verify stream is closed (try-with-resources ensures this)
      verify(inputStream, atLeastOnce()).close();
    }
  }

  @Nested
  @DisplayName("readFileContents() - Edge cases")
  class EdgeCases {

    @Test
    @DisplayName("Should handle file with only line breaks")
    void shouldHandleFileWithOnlyLineBreaks() throws IOException {
      String fileContent = "\n\n\n";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(TEST_FILE_NAME);

      assertNotNull(result);
      // Should contain multiple line separators
      assertTrue(result.contains(System.lineSeparator()));
    }

    @Test
    @DisplayName("Should handle file with mixed line endings")
    void shouldHandleFileWithMixedLineEndings() throws IOException {
      String fileContent = "Line 1\r\nLine 2\nLine 3\rLine 4";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(TEST_FILE_NAME);

      assertNotNull(result);
      assertTrue(result.contains("Line 1"));
      assertTrue(result.contains("Line 2"));
      assertTrue(result.contains("Line 3"));
      assertTrue(result.contains("Line 4"));
    }

    @Test
    @DisplayName("Should handle file with very long lines")
    void shouldHandleFileWithVeryLongLines() throws IOException {
      String longLine = "a".repeat(10000);
      String fileContent = longLine + "\nShort line";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + TEST_FILE_NAME)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(TEST_FILE_NAME);

      assertNotNull(result);
      assertTrue(result.contains("Short line"));
      assertTrue(result.length() > 10000);
    }

    @Test
    @DisplayName("Should handle file name with spaces")
    void shouldHandleFileNameWithSpaces() throws IOException {
      String fileName = "file with spaces.txt";
      String fileContent = "Content";
      InputStream inputStream =
          new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

      when(resourceLoader.getResource(CLASSPATH_PREFIX + fileName)).thenReturn(resource);
      when(resource.getInputStream()).thenReturn(inputStream);

      String result = fileReaderUtils.readFileContents(fileName);

      assertNotNull(result);
      verify(resourceLoader).getResource(CLASSPATH_PREFIX + fileName);
    }
  }
}
