package account.integration;

import account.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("JWT Integration Tests")
class JwtIntegrationTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private JwtUtil jwtUtil;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("登入成功後應該回傳有效的JWT token包含正確的用戶資訊")
  void shouldReturnValidJwtTokenWithCorrectUserInfo() throws Exception {
    // Given - 註冊一個測試用戶
    String testEmail = "jwt-test@example.com";
    String testUsername = "jwtuser";
    String testPassword = "password123";

    Map<String, String> registerRequest = Map.of(
        "email", testEmail,
        "password", testPassword,
        "username", testUsername);

    webTestClient.post()
        .uri("/account/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(registerRequest)
        .exchange()
        .expectStatus().isCreated();

    // When - 登入
    Map<String, String> loginRequest = Map.of(
        "email", testEmail,
        "password", testPassword);

    String responseBody = webTestClient.post()
        .uri("/account/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(loginRequest)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Then - 驗證回應結構
    JsonNode response = objectMapper.readTree(responseBody);

    assertNotNull(response.get("accessToken"));
    assertEquals("Bearer", response.get("tokenType").asText());
    assertEquals(3600, response.get("expiresIn").asInt());

    JsonNode user = response.get("user");
    assertNotNull(user.get("id"));
    assertEquals(testEmail, user.get("email").asText());
    assertEquals(testUsername, user.get("username").asText());
    assertNotNull(user.get("createdAt"));
    assertNotNull(user.get("updatedAt"));

    // 驗證JWT token內容
    String accessToken = response.get("accessToken").asText();
    assertNotNull(accessToken);
    assertFalse(accessToken.isEmpty());

    // 解析JWT token
    String extractedUserId = jwtUtil.extractUserId(accessToken);
    String extractedEmail = jwtUtil.extractEmail(accessToken);
    String extractedUsername = jwtUtil.extractUsername(accessToken);

    assertEquals(user.get("id").asText(), extractedUserId);
    assertEquals(testEmail, extractedEmail);
    assertEquals(testUsername, extractedUsername);

    // 驗證token有效性
    assertTrue(jwtUtil.validateToken(accessToken, extractedUserId));
    assertFalse(jwtUtil.isTokenExpired(accessToken));
  }

  @Test
  @DisplayName("JWT token應該包含正確的用戶ID格式")
  void shouldContainCorrectUserIdFormat() throws Exception {
    // Given
    String testEmail = "jwt-format-test@example.com";
    String testUsername = "formatuser";
    String testPassword = "password123";

    Map<String, String> registerRequest = Map.of(
        "email", testEmail,
        "password", testPassword,
        "username", testUsername);

    webTestClient.post()
        .uri("/account/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(registerRequest)
        .exchange()
        .expectStatus().isCreated();

    // When
    Map<String, String> loginRequest = Map.of(
        "email", testEmail,
        "password", testPassword);

    String responseBody = webTestClient.post()
        .uri("/account/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(loginRequest)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Then
    JsonNode response = objectMapper.readTree(responseBody);
    String userId = response.get("user").get("id").asText();
    String accessToken = response.get("accessToken").asText();

    // 驗證用戶ID格式 (應該以 "usr_" 開頭)
    assertTrue(userId.startsWith("usr_"));
    assertTrue(userId.length() > 4);

    // 驗證JWT中的用戶ID與回應中的一致
    String jwtUserId = jwtUtil.extractUserId(accessToken);
    assertEquals(userId, jwtUserId);
  }
}