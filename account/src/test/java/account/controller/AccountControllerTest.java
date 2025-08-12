package account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("Account API Tests")
class AccountControllerTest {

        @Autowired
        private WebTestClient webTestClient;

        @Autowired
        private ObjectMapper objectMapper;

        @Nested
        @DisplayName("註冊 Register Tests")
        class RegisterTests {

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
                                        .jsonPath("$.user.createdAt").exists();
                }

                @Test
                @DisplayName("註冊時 email 格式無效應回傳 400")
                void shouldReturn400ForInvalidEmail() throws Exception {
                        Map<String, String> registerRequest = Map.of(
                                        "email", "invalid-email",
                                        "password", "password123",
                                        "username", "testuser");

                        webTestClient.post()
                                        .uri("/api/account/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(registerRequest)
                                        .exchange()
                                        .expectStatus().isBadRequest()
                                        .expectBody()
                                        .jsonPath("$.error").isEqualTo("validation_error")
                                        .jsonPath("$.message").exists()
                                        .jsonPath("$.details.email").exists();
                }

                @Test
                @DisplayName("註冊時密碼長度不足應回傳 400")
                void shouldReturn400ForShortPassword() throws Exception {
                        Map<String, String> registerRequest = Map.of(
                                        "email", "test@example.com",
                                        "password", "123456", // 少於 7 字元
                                        "username", "testuser");

                        webTestClient.post()
                                        .uri("/api/account/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(registerRequest)
                                        .exchange()
                                        .expectStatus().isBadRequest()
                                        .expectBody()
                                        .jsonPath("$.error").isEqualTo("validation_error")
                                        .jsonPath("$.details.password").exists();
                }

                @Test
                @DisplayName("註冊時 username 長度不符應回傳 400")
                void shouldReturn400ForInvalidUsername() throws Exception {
                        Map<String, String> registerRequest = Map.of(
                                        "email", "test@example.com",
                                        "password", "password123",
                                        "username", "ab" // 少於 3 字元
                        );

                        webTestClient.post()
                                        .uri("/api/account/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(registerRequest)
                                        .exchange()
                                        .expectStatus().isBadRequest()
                                        .expectBody()
                                        .jsonPath("$.error").isEqualTo("validation_error")
                                        .jsonPath("$.details.username").exists();
                }

                @Test
                @DisplayName("註冊重複 email 應回傳 409")
                void shouldReturn409ForDuplicateEmail() throws Exception {
                        // 先註冊一個帳號
                        Map<String, String> firstRequest = Map.of(
                                        "email", "duplicate@example.com",
                                        "password", "password123",
                                        "username", "user1");

                        webTestClient.post()
                                        .uri("/api/account/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(firstRequest)
                                        .exchange()
                                        .expectStatus().isCreated();

                        // 再次註冊相同 email
                        Map<String, String> duplicateRequest = Map.of(
                                        "email", "duplicate@example.com",
                                        "password", "password456",
                                        "username", "user2");

                        webTestClient.post()
                                        .uri("/api/account/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(duplicateRequest)
                                        .exchange()
                                        .expectStatus().isEqualTo(409)
                                        .expectBody()
                                        .jsonPath("$.error").isEqualTo("conflict")
                                        .jsonPath("$.message").isEqualTo("email already registered");
                }
        }

        @Nested
        @DisplayName("登入 Login Tests")
        class LoginTests {

                @BeforeEach
                void setUp() throws Exception {
                        // 註冊測試用帳號
                        Map<String, String> registerRequest = Map.of(
                                        "email", "login@example.com",
                                        "password", "password123",
                                        "username", "loginuser");

                        webTestClient.post()
                                        .uri("/api/account/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(registerRequest)
                                        .exchange()
                                        .expectStatus().isCreated();
                }

                @Test
                @DisplayName("成功登入應回傳 JWT token")
                void shouldLoginSuccessfully() throws Exception {
                        Map<String, String> loginRequest = Map.of(
                                        "email", "login@example.com",
                                        "password", "password123");

                        webTestClient.post()
                                        .uri("/api/account/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(loginRequest)
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectBody()
                                        .jsonPath("$.accessToken").exists()
                                        .jsonPath("$.refreshToken").exists()
                                        .jsonPath("$.tokenType").isEqualTo("Bearer")
                                        .jsonPath("$.expiresIn").isEqualTo(3600)
                                        .jsonPath("$.user.email").isEqualTo("login@example.com")
                                        .jsonPath("$.user.username").isEqualTo("loginuser")
                                        .jsonPath("$.user.id").exists();
                }

                @Test
                @DisplayName("錯誤密碼應回傳 401")
                void shouldReturn401ForWrongPassword() throws Exception {
                        Map<String, String> loginRequest = Map.of(
                                        "email", "login@example.com",
                                        "password", "wrongpassword");

                        webTestClient.post()
                                        .uri("/api/account/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(loginRequest)
                                        .exchange()
                                        .expectStatus().isUnauthorized()
                                        .expectBody()
                                        .jsonPath("$.error").isEqualTo("invalid_credentials")
                                        .jsonPath("$.message").isEqualTo("email or password is incorrect");
                }

                @Test
                @DisplayName("不存在的 email 應回傳 401")
                void shouldReturn401ForNonExistentEmail() throws Exception {
                        Map<String, String> loginRequest = Map.of(
                                        "email", "nonexistent@example.com",
                                        "password", "password123");

                        webTestClient.post()
                                        .uri("/api/account/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(loginRequest)
                                        .exchange()
                                        .expectStatus().isUnauthorized()
                                        .expectBody()
                                        .jsonPath("$.error").isEqualTo("invalid_credentials")
                                        .jsonPath("$.message").isEqualTo("email or password is incorrect");
                }

                @Test
                @DisplayName("無效 email 格式應回傳 400")
                void shouldReturn400ForInvalidEmailFormat() throws Exception {
                        Map<String, String> loginRequest = Map.of(
                                        "email", "invalid-email",
                                        "password", "password123");

                        webTestClient.post()
                                        .uri("/api/account/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(loginRequest)
                                        .exchange()
                                        .expectStatus().isBadRequest()
                                        .expectBody()
                                        .jsonPath("$.error").isEqualTo("validation_error");
                }
        }

        @Nested
        @DisplayName("查詢使用者 Get User Tests")
        class GetUserTests {

                private String accessToken;
                private String userId;

                @BeforeEach
                void setUp() throws Exception {
                        // 註冊並登入取得 token
                        Map<String, String> registerRequest = Map.of(
                                        "email", "getuser@example.com",
                                        "password", "password123",
                                        "username", "getuser");

                        webTestClient.post()
                                        .uri("/api/account/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(registerRequest)
                                        .exchange()
                                        .expectStatus().isCreated();

                        Map<String, String> loginRequest = Map.of(
                                        "email", "getuser@example.com",
                                        "password", "password123");

                        var loginResponse = webTestClient.post()
                                        .uri("/api/account/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(loginRequest)
                                        .exchange()
                                        .expectStatus().isOk()
                                        .returnResult(String.class)
                                        .getResponseBody()
                                        .blockFirst();

                        // 解析回應取得 token 和 userId (實際實作中需要 JSON 解析)
                        // 這裡假設有方法可以解析
                        this.accessToken = "mock-access-token"; // 實際需要從回應解析
                        this.userId = "usr_01HXYZ"; // 實際需要從回應解析
                }

                @Test
                @DisplayName("成功查詢自己的資訊 (/me)")
                void shouldGetOwnUserInfo() throws Exception {
                        webTestClient.get()
                                        .uri("/api/account/me")
                                        .header("Authorization", "Bearer " + accessToken)
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectBody()
                                        .jsonPath("$.id").exists()
                                        .jsonPath("$.email").isEqualTo("getuser@example.com")
                                        .jsonPath("$.username").isEqualTo("getuser")
                                        .jsonPath("$.createdAt").exists()
                                        .jsonPath("$.updatedAt").exists();
                }

                @Test
                @DisplayName("成功查詢指定使用者資訊")
                void shouldGetUserById() throws Exception {
                        webTestClient.get()
                                        .uri("/api/account/users/" + userId)
                                        .header("Authorization", "Bearer " + accessToken)
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectBody()
                                        .jsonPath("$.id").isEqualTo(userId)
                                        .jsonPath("$.email").isEqualTo("getuser@example.com")
                                        .jsonPath("$.username").isEqualTo("getuser");
                }

                @Test
                @DisplayName("無 token 查詢應回傳 401")
                void shouldReturn401WithoutToken() throws Exception {
                        webTestClient.get()
                                        .uri("/api/account/me")
                                        .exchange()
                                        .expectStatus().isUnauthorized();
                }

                @Test
                @DisplayName("無效 token 查詢應回傳 401")
                void shouldReturn401WithInvalidToken() throws Exception {
                        webTestClient.get()
                                        .uri("/api/account/me")
                                        .header("Authorization", "Bearer invalid-token")
                                        .exchange()
                                        .expectStatus().isUnauthorized();
                }

                @Test
                @DisplayName("查詢不存在的使用者應回傳 404")
                void shouldReturn404ForNonExistentUser() throws Exception {
                        webTestClient.get()
                                        .uri("/api/account/users/usr_nonexistent")
                                        .header("Authorization", "Bearer " + accessToken)
                                        .exchange()
                                        .expectStatus().isNotFound()
                                        .expectBody()
                                        .jsonPath("$.error").isEqualTo("not_found")
                                        .jsonPath("$.message").isEqualTo("user not found");
                }
        }

}