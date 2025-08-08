package com.example.account.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.MediaType;

import java.util.Map;

/**
 * 測試工具類別，提供常用的測試操作方法
 */
public class TestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 註冊測試用戶
     */
    public static WebTestClient.ResponseSpec registerUser(WebTestClient webTestClient, 
                                                         String email, 
                                                         String password, 
                                                         String username) {
        Map<String, String> registerRequest = Map.of(
            "email", email,
            "password", password,
            "username", username
        );

        return webTestClient.post()
            .uri("/api/account/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerRequest)
            .exchange();
    }

    /**
     * 登入並取得 access token
     */
    public static String loginAndGetToken(WebTestClient webTestClient, 
                                        String email, 
                                        String password) throws Exception {
        Map<String, String> loginRequest = Map.of(
            "email", email,
            "password", password
        );

        String responseBody = webTestClient.post()
            .uri("/api/account/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("accessToken").asText();
    }

    /**
     * 註冊並登入，返回 access token
     */
    public static String registerAndLogin(WebTestClient webTestClient,
                                        String email,
                                        String password,
                                        String username) throws Exception {
        // 註冊
        registerUser(webTestClient, email, password, username)
            .expectStatus().isCreated();

        // 登入並返回 token
        return loginAndGetToken(webTestClient, email, password);
    }

    /**
     * 生成測試用的 email
     */
    public static String generateTestEmail(String prefix) {
        return prefix + System.currentTimeMillis() + "@test.com";
    }

    /**
     * 生成測試用的 username
     */
    public static String generateTestUsername(String prefix) {
        return prefix + System.currentTimeMillis();
    }

    /**
     * 驗證錯誤回應格式
     */
    public static void expectErrorResponse(WebTestClient.BodyContentSpec bodySpec,
                                         String expectedError) {
        bodySpec
            .jsonPath("$.error").isEqualTo(expectedError)
            .jsonPath("$.message").exists();
    }

    /**
     * 驗證錯誤回應格式（包含詳細資訊）
     */
    public static void expectValidationErrorResponse(WebTestClient.BodyContentSpec bodySpec) {
        bodySpec
            .jsonPath("$.error").isEqualTo("validation_error")
            .jsonPath("$.message").exists()
            .jsonPath("$.details").exists();
    }

    /**
     * 驗證使用者回應格式
     */
    public static void expectUserResponse(WebTestClient.BodyContentSpec bodySpec,
                                        String expectedEmail,
                                        String expectedUsername) {
        bodySpec
            .jsonPath("$.id").exists()
            .jsonPath("$.email").isEqualTo(expectedEmail)
            .jsonPath("$.username").isEqualTo(expectedUsername)
            .jsonPath("$.createdAt").exists();
    }

    /**
     * 驗證登入回應格式
     */
    public static void expectLoginResponse(WebTestClient.BodyContentSpec bodySpec,
                                         String expectedEmail,
                                         String expectedUsername) {
        bodySpec
            .jsonPath("$.accessToken").exists()
            .jsonPath("$.refreshToken").exists()
            .jsonPath("$.tokenType").isEqualTo("Bearer")
            .jsonPath("$.expiresIn").isEqualTo(3600)
            .jsonPath("$.user.email").isEqualTo(expectedEmail)
            .jsonPath("$.user.username").isEqualTo(expectedUsername)
            .jsonPath("$.user.id").exists();
    }

    /**
     * 建立 Authorization header
     */
    public static String bearerToken(String token) {
        return "Bearer " + token;
    }

    /**
     * 常用的測試資料
     */
    public static class TestData {
        public static final String VALID_EMAIL = "test@example.com";
        public static final String VALID_PASSWORD = "password123";
        public static final String VALID_USERNAME = "testuser";
        public static final String VALID_NICKNAME = "Test User";
        
        public static final String INVALID_EMAIL = "invalid-email";
        public static final String SHORT_PASSWORD = "123456";
        public static final String SHORT_USERNAME = "ab";
        public static final String LONG_USERNAME = "a".repeat(33);
        public static final String LONG_NICKNAME = "a".repeat(33);
    }
}