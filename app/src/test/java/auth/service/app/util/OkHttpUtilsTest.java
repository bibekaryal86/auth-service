package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class OkHttpUtilsTest extends BaseTest {

  private static MockWebServer mockWebServer;

  @BeforeAll
  static void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start(0);
  }

  @AfterAll
  static void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  @Test
  void testSendRequest_Success() {
    String responseBody = "{\"message\":\"success\"}";
    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setBody(responseBody)
            .addHeader("Content-Type", "application/json"));

    String url = mockWebServer.url("/test").toString();
    OkHttpUtils.HttpResponse response = OkHttpUtils.sendRequest(url, "GET", null, null, null);

    assertEquals(200, response.statusCode());
    assertEquals(responseBody, response.responseBody());
  }

  @Test
  void testSendRequest_ServerError() {
    mockWebServer.enqueue(new MockResponse().setResponseCode(500));

    String url = mockWebServer.url("/error").toString();
    OkHttpUtils.HttpResponse response = OkHttpUtils.sendRequest(url, "GET", null, null, null);

    assertEquals(500, response.statusCode());
    assertEquals("", response.responseBody());
  }

  @Test
  void testSendRequest_Exception() {
    String invalidUrl = "http://invalid-url";

    OkHttpUtils.HttpResponse response =
        OkHttpUtils.sendRequest(invalidUrl, "GET", null, null, null);

    assertEquals(-1, response.statusCode());
    assertTrue(response.responseBody().contains("errMsg"));
  }

  @Test
  void testSendRequest_WithHeadersAndAuth() throws Exception {
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("Authorized"));

    String url = mockWebServer.url("/auth").toString();
    String authorization = OkHttpUtils.createBasicAuthHeader("user", "pass");
    Map<String, String> headers = Map.of("Custom-Header", "HeaderValue");

    OkHttpUtils.HttpResponse response =
        OkHttpUtils.sendRequest(url, "POST", "{\"key\":\"value\"}", headers, authorization);

    assertEquals(200, response.statusCode());
    assertEquals("Authorized", response.responseBody());

    var recordedRequest = mockWebServer.takeRequest();
    assertEquals("POST", recordedRequest.getMethod());
    assertEquals("HeaderValue", recordedRequest.getHeader("Custom-Header"));
    assertEquals(authorization, recordedRequest.getHeader("Authorization"));
    assertEquals("{\"key\":\"value\"}", recordedRequest.getBody().readUtf8());
  }

  @Test
  void testCreateBasicAuthHeader() {
    String username = "testUser";
    String password = "testPass";

    String header = OkHttpUtils.createBasicAuthHeader(username, password);

    assertTrue(header.startsWith("Basic "));
    String base64Credentials = header.substring(6);
    String decoded = new String(java.util.Base64.getDecoder().decode(base64Credentials));
    assertEquals(username + ":" + password, decoded);
  }
}
