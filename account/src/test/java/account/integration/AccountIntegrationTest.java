package account.integration;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("Account Service Integration Tests")
class AccountIntegrationTest {

        @Autowired
        private WebTestClient webTestClient;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("完整用戶流程：註冊 -> 登入 -> 查詢資訊")
        void shouldCompleteUserFlow() throws Exception {
                String email = "integration@example.com";
                String password = "password123";
                String username = "integrationuser";

                // 1. 註冊
                Map<String, String> registerRequest = Map.of(
                                "email", email,
                                "password", password,
                                "username", username);

                var registerResponse = webTestClient.post()
                                .uri("/account/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(registerRequest)
                                .exchange()
                                .expectStatus().isCreated()
                                .expectBody()
                                .jsonPath("$.msg").isEqualTo("register success")
                                .jsonPath("$.user.email").isEqualTo(email)
                                .jsonPath("$.user.username").isEqualTo(username)
                                .returnResult()
                                .getResponseBody();

                // 2. 登入
                Map<String, String> loginRequest = Map.of(
                                "email", email,
                                "password", password);

                var loginResponseBody = webTestClient.post()
                                .uri("/account/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loginRequest)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(String.class)
                                .returnResult()
                                .getResponseBody();

                // 解析登入回應取得 token (實際實作中需要完整的 JSON 解析)
                JsonNode loginJson = objectMapper.readTree(loginResponseBody);
                String accessToken = loginJson.get("accessToken").asText();
                String userId = loginJson.get("user").get("id").asText();

                // 3. 查詢自己的資訊
                webTestClient.get()
                                .uri("/account/me")
                                .header("Authorization", "Bearer " + accessToken)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.email").isEqualTo(email)
                                .jsonPath("$.username").isEqualTo(username);

                // 4. 測試完成 - 移除暱稱更新功能
        }

        @Test
        @DisplayName("測試錯誤處理流程")
        void shouldHandleErrorsCorrectly() throws Exception {
                // 1. 嘗試用無效資料註冊
                Map<String, String> invalidRegisterRequest = Map.of(
                                "email", "invalid-email",
                                "password", "123", // 太短
                                "username", "ab" // 太短
                );

                webTestClient.post()
                                .uri("/account/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(invalidRegisterRequest)
                                .exchange()
                                .expectStatus().isBadRequest()
                                .expectBody()
                                .jsonPath("$.error").isEqualTo("validation_error")
                                .jsonPath("$.details").exists();

                // 2. 嘗試用不存在的帳號登入
                Map<String, String> invalidLoginRequest = Map.of(
                                "email", "nonexistent@example.com",
                                "password", "password123");

                webTestClient.post()
                                .uri("/account/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(invalidLoginRequest)
                                .exchange()
                                .expectStatus().isUnauthorized()
                                .expectBody()
                                .jsonPath("$.error").isEqualTo("invalid_credentials");

                // 3. 嘗試無 token 存取受保護資源
                webTestClient.get()
                                .uri("/account/me")
                                .exchange()
                                .expectStatus().isUnauthorized();

                // 4. 嘗試用無效 token 存取
                webTestClient.get()
                                .uri("/account/me")
                                .header("Authorization", "Bearer invalid-token")
                                .exchange()
                                .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("測試重複註冊處理")
        void shouldHandleDuplicateRegistration() throws Exception {
                String email = "duplicate-test@example.com";

                // 第一次註冊
                Map<String, String> firstRequest = Map.of(
                                "email", email,
                                "password", "password123",
                                "username", "user1");

                webTestClient.post()
                                .uri("/account/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(firstRequest)
                                .exchange()
                                .expectStatus().isCreated();

                // 第二次註冊相同 email
                Map<String, String> duplicateRequest = Map.of(
                                "email", email,
                                "password", "password456",
                                "username", "user2");

                webTestClient.post()
                                .uri("/account/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(duplicateRequest)
                                .exchange()
                                .expectStatus().isEqualTo(409)
                                .expectBody()
                                .jsonPath("$.error").isEqualTo("conflict")
                                .jsonPath("$.message").isEqualTo("email already registered");
        }
}