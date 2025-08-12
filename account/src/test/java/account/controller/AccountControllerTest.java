package account.controller;

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

        @Nested
        @DisplayName("註冊 Register Tests")
        class RegisterTests {

                @Test
                @DisplayName("成功註冊新帳號")
                void shouldRegisterSuccessfully() throws Exception {
                        Map<String, String> registerRequest = Map.of(
                                        "email", "test456@example.com",
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
                                        .jsonPath("$.user.email").isEqualTo("test456@example.com")
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
                                        .uri("/account/register")
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
                                        .uri("/account/register")
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
                                        .uri("/account/register")
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
                                        .uri("/account/register")
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

        @Nested
        @DisplayName("登入 Login Tests")
        class LoginTests {

                private void registerTestUser(String email, String username) {
                        Map<String, String> registerRequest = Map.of(
                                        "email", email,
                                        "password", "password123",
                                        "username", username);

                        webTestClient.post()
                                        .uri("/account/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(registerRequest)
                                        .exchange()
                                        .expectStatus().isCreated();
                }

                @Test
                @DisplayName("成功登入應回傳 JWT token")
                void shouldLoginSuccessfully() throws Exception {
                        // 註冊測試用帳號
                        registerTestUser("login-success@example.com", "loginuser1");

                        Map<String, String> loginRequest = Map.of(
                                        "email", "login-success@example.com",
                                        "password", "password123");

                        webTestClient.post()
                                        .uri("/account/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(loginRequest)
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectBody()
                                        .jsonPath("$.accessToken").exists()
                                        .jsonPath("$.refreshToken").exists()
                                        .jsonPath("$.tokenType").isEqualTo("Bearer")
                                        .jsonPath("$.expiresIn").isEqualTo(3600)
                                        .jsonPath("$.user.email").isEqualTo("login-success@example.com")
                                        .jsonPath("$.user.username").isEqualTo("loginuser1")
                                        .jsonPath("$.user.id").exists();
                }

                @Test
                @DisplayName("錯誤密碼應回傳 401")
                void shouldReturn401ForWrongPassword() throws Exception {
                        // 註冊測試用帳號
                        registerTestUser("login-wrong-pwd@example.com", "loginuser2");

                        Map<String, String> loginRequest = Map.of(
                                        "email", "login-wrong-pwd@example.com",
                                        "password", "wrongpassword");

                        webTestClient.post()
                                        .uri("/account/login")
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
                                        .uri("/account/login")
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
                                        .uri("/account/login")
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

                private String registerAndLoginUser(String email, String username) {
                        // 註冊帳號
                        Map<String, String> registerRequest = Map.of(
                                        "email", email,
                                        "password", "password123",
                                        "username", username);

                        webTestClient.post()
                                        .uri("/account/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(registerRequest)
                                        .exchange()
                                        .expectStatus().isCreated();

                        // 登入取得 token (這裡先用 mock token，實際需要解析 JSON)
                        Map<String, String> loginRequest = Map.of(
                                        "email", email,
                                        "password", "password123");

                        webTestClient.post()
                                        .uri("/account/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(loginRequest)
                                        .exchange()
                                        .expectStatus().isOk();

                        return "mock-access-token"; // 實際需要從回應解析
                }

                @Test
                @DisplayName("成功查詢自己的資訊 (/me)")
                void shouldGetOwnUserInfo() throws Exception {
                        String accessToken = registerAndLoginUser("getuser-me@example.com", "getuser1");

                        webTestClient.get()
                                        .uri("/account/me")
                                        .header("Authorization", "Bearer " + accessToken)
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectBody()
                                        .jsonPath("$.id").exists()
                                        .jsonPath("$.email").isEqualTo("getuser-me@example.com")
                                        .jsonPath("$.username").isEqualTo("getuser1")
                                        .jsonPath("$.createdAt").exists()
                                        .jsonPath("$.updatedAt").exists();
                }

                @Test
                @DisplayName("成功查詢指定使用者資訊")
                void shouldGetUserById() throws Exception {
                        String accessToken = registerAndLoginUser("getuser-byid@example.com", "getuser2");
                        String userId = "usr_01HXYZ"; // 實際需要從註冊回應解析

                        webTestClient.get()
                                        .uri("/account/users/" + userId)
                                        .header("Authorization", "Bearer " + accessToken)
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectBody()
                                        .jsonPath("$.id").isEqualTo(userId)
                                        .jsonPath("$.email").isEqualTo("getuser-byid@example.com")
                                        .jsonPath("$.username").isEqualTo("getuser2");
                }

                @Test
                @DisplayName("無 token 查詢應回傳 401")
                void shouldReturn401WithoutToken() throws Exception {
                        webTestClient.get()
                                        .uri("/account/me")
                                        .exchange()
                                        .expectStatus().isUnauthorized();
                }

                @Test
                @DisplayName("無效 token 查詢應回傳 401")
                void shouldReturn401WithInvalidToken() throws Exception {
                        webTestClient.get()
                                        .uri("/account/me")
                                        .header("Authorization", "Bearer invalid-token")
                                        .exchange()
                                        .expectStatus().isUnauthorized();
                }

                @Test
                @DisplayName("查詢不存在的使用者應回傳 404")
                void shouldReturn404ForNonExistentUser() throws Exception {
                        String accessToken = registerAndLoginUser("getuser-404@example.com", "getuser3");

                        webTestClient.get()
                                        .uri("/account/users/usr_nonexistent")
                                        .header("Authorization", "Bearer " + accessToken)
                                        .exchange()
                                        .expectStatus().isNotFound()
                                        .expectBody()
                                        .jsonPath("$.error").isEqualTo("not_found")
                                        .jsonPath("$.message").isEqualTo("user not found");
                }
        }

}