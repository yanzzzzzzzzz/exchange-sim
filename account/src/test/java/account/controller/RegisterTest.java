package account.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("Register API Tests")
class RegisterTest {

  @Autowired
  private WebTestClient webTestClient;

  @Test
  @DisplayName("成功註冊新帳號")
  void shouldRegisterSuccessfully() throws Exception {
    Map<String, String> registerRequest = Map.of(
        "email", "test@example.com",
        "password", "password123",
        "username", "testuser");

    webTestClient.post()
        .uri("/account/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(registerRequest)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.msg").isEqualTo("register success")
        .jsonPath("$.user.email").isEqualTo("test@example.com")
        .jsonPath("$.user.username").isEqualTo("testuser")
        .jsonPath("$.user.id").exists()
        .jsonPath("$.user.createdAt").exists()
        .jsonPath("$.user.updatedAt").exists();
  }

  @Test
  @DisplayName("註冊時缺少必填欄位應回傳 400")
  void shouldReturn400ForMissingFields() throws Exception {
    Map<String, String> registerRequest = Map.of(
        "email", "test@example.com"
    // 缺少 username 和 password
    );

    webTestClient.post()
        .uri("/account/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(registerRequest)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.error").isEqualTo("validation_error")
        .jsonPath("$.message").isEqualTo("invalid fields")
        .jsonPath("$.details").exists();
  }
}