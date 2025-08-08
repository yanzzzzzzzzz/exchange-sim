package com.example.account.controller;

import com.example.account.util.TestUtils;
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
@DisplayName("Account API Tests with Utils")
class AccountControllerWithUtilsTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("使用工具類別測試完整註冊流程")
    void shouldRegisterWithUtils() throws Exception {
        String email = TestUtils.generateTestEmail("register");
        String username = TestUtils.generateTestUsername("user");

        TestUtils.registerUser(webTestClient, email, TestUtils.TestData.VALID_PASSWORD, username)
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.msg").isEqualTo("register success")
            .jsonPath("$.user.email").isEqualTo(email)
            .jsonPath("$.user.username").isEqualTo(username)
            .jsonPath("$.user.id").exists()
            .jsonPath("$.user.createdAt").exists();
    }

    @Test
    @DisplayName("使用工具類別測試登入流程")
    void shouldLoginWithUtils() throws Exception {
        String email = TestUtils.generateTestEmail("login");
        String username = TestUtils.generateTestUsername("user");

        // 註冊
        TestUtils.registerUser(webTestClient, email, TestUtils.TestData.VALID_PASSWORD, username)
            .expectStatus().isCreated();

        // 登入
        Map<String, String> loginRequest = Map.of(
            "email", email,
            "password", TestUtils.TestData.VALID_PASSWORD
        );

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
            .jsonPath("$.user.email").isEqualTo(email)
            .jsonPath("$.user.username").isEqualTo(username)
            .jsonPath("$.user.id").exists();
    }

    @Test
    @DisplayName("使用工具類別測試驗證錯誤")
    void shouldHandleValidationErrorsWithUtils() throws Exception {
        Map<String, String> invalidRequest = Map.of(
            "email", TestUtils.TestData.INVALID_EMAIL,
            "password", TestUtils.TestData.SHORT_PASSWORD,
            "username", TestUtils.TestData.SHORT_USERNAME
        );

        webTestClient.post()
            .uri("/api/account/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.error").isEqualTo("validation_error")
            .jsonPath("$.message").exists()
            .jsonPath("$.details").exists();
    }

    @Test
    @DisplayName("使用工具類別測試完整用戶操作流程")
    void shouldCompleteUserFlowWithUtils() throws Exception {
        String email = TestUtils.generateTestEmail("flow");
        String username = TestUtils.generateTestUsername("user");

        // 註冊並登入取得 token
        String accessToken = TestUtils.registerAndLogin(webTestClient, email, 
            TestUtils.TestData.VALID_PASSWORD, username);

        // 查詢自己的資訊
        webTestClient.get()
            .uri("/api/account/me")
            .header("Authorization", TestUtils.bearerToken(accessToken))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.email").isEqualTo(email)
            .jsonPath("$.username").isEqualTo(username)
            .jsonPath("$.createdAt").exists();

        // 更新暱稱
        Map<String, String> nicknameRequest = Map.of(
            "nickname", TestUtils.TestData.VALID_NICKNAME
        );

        webTestClient.patch()
            .uri("/api/account/me/nickname")
            .header("Authorization", TestUtils.bearerToken(accessToken))
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(nicknameRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.msg").isEqualTo("nickname updated")
            .jsonPath("$.user.nickname").isEqualTo(TestUtils.TestData.VALID_NICKNAME);
    }

    @Test
    @DisplayName("測試未授權存取")
    void shouldRejectUnauthorizedAccess() throws Exception {
        // 無 token 存取
        webTestClient.get()
            .uri("/api/account/me")
            .exchange()
            .expectStatus().isUnauthorized();

        // 無效 token 存取
        webTestClient.get()
            .uri("/api/account/me")
            .header("Authorization", TestUtils.bearerToken("invalid-token"))
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("測試重複註冊處理")
    void shouldHandleDuplicateRegistration() throws Exception {
        String email = TestUtils.generateTestEmail("duplicate");
        String username1 = TestUtils.generateTestUsername("user1");
        String username2 = TestUtils.generateTestUsername("user2");

        // 第一次註冊
        TestUtils.registerUser(webTestClient, email, TestUtils.TestData.VALID_PASSWORD, username1)
            .expectStatus().isCreated();

        // 第二次註冊相同 email
        TestUtils.registerUser(webTestClient, email, TestUtils.TestData.VALID_PASSWORD, username2)
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.error").isEqualTo("conflict")
            .jsonPath("$.message").exists();
    }
}