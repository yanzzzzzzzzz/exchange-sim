package account.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Util Tests")
class JwtUtilTest {

  private JwtUtil jwtUtil;
  private final String testSecret = "myTestSecretKeyForJWTTokenGenerationThatIsLongEnough";
  private final long testExpiration = 3600; // 1 hour

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil(testSecret, testExpiration);
  }

  @Test
  @DisplayName("應該成功生成JWT token")
  void shouldGenerateJwtToken() {
    // Given
    String userId = "usr_123456";
    String email = "test@example.com";
    String username = "testuser";

    // When
    String token = jwtUtil.generateToken(userId, email, username);

    // Then
    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.contains("."));
  }

  @Test
  @DisplayName("應該能從token中提取用戶ID")
  void shouldExtractUserIdFromToken() {
    // Given
    String userId = "usr_123456";
    String email = "test@example.com";
    String username = "testuser";
    String token = jwtUtil.generateToken(userId, email, username);

    // When
    String extractedUserId = jwtUtil.extractUserId(token);

    // Then
    assertEquals(userId, extractedUserId);
  }

  @Test
  @DisplayName("應該能從token中提取email")
  void shouldExtractEmailFromToken() {
    // Given
    String userId = "usr_123456";
    String email = "test@example.com";
    String username = "testuser";
    String token = jwtUtil.generateToken(userId, email, username);

    // When
    String extractedEmail = jwtUtil.extractEmail(token);

    // Then
    assertEquals(email, extractedEmail);
  }

  @Test
  @DisplayName("應該能從token中提取username")
  void shouldExtractUsernameFromToken() {
    // Given
    String userId = "usr_123456";
    String email = "test@example.com";
    String username = "testuser";
    String token = jwtUtil.generateToken(userId, email, username);

    // When
    String extractedUsername = jwtUtil.extractUsername(token);

    // Then
    assertEquals(username, extractedUsername);
  }

  @Test
  @DisplayName("應該能提取所有claims")
  void shouldExtractAllClaims() {
    // Given
    String userId = "usr_123456";
    String email = "test@example.com";
    String username = "testuser";
    String token = jwtUtil.generateToken(userId, email, username);

    // When
    Claims claims = jwtUtil.extractClaims(token);

    // Then
    assertEquals(userId, claims.getSubject());
    assertEquals(email, claims.get("email", String.class));
    assertEquals(username, claims.get("username", String.class));
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());
  }

  @Test
  @DisplayName("新生成的token不應該過期")
  void shouldNotBeExpiredForNewToken() {
    // Given
    String userId = "usr_123456";
    String email = "test@example.com";
    String username = "testuser";
    String token = jwtUtil.generateToken(userId, email, username);

    // When
    boolean isExpired = jwtUtil.isTokenExpired(token);

    // Then
    assertFalse(isExpired);
  }

  @Test
  @DisplayName("應該驗證有效的token")
  void shouldValidateValidToken() {
    // Given
    String userId = "usr_123456";
    String email = "test@example.com";
    String username = "testuser";
    String token = jwtUtil.generateToken(userId, email, username);

    // When
    boolean isValid = jwtUtil.validateToken(token, userId);

    // Then
    assertTrue(isValid);
  }

  @Test
  @DisplayName("不應該驗證錯誤用戶ID的token")
  void shouldNotValidateTokenWithWrongUserId() {
    // Given
    String userId = "usr_123456";
    String wrongUserId = "usr_654321";
    String email = "test@example.com";
    String username = "testuser";
    String token = jwtUtil.generateToken(userId, email, username);

    // When
    boolean isValid = jwtUtil.validateToken(token, wrongUserId);

    // Then
    assertFalse(isValid);
  }

  @Test
  @DisplayName("應該拒絕無效的token")
  void shouldRejectInvalidToken() {
    // Given
    String invalidToken = "invalid.token.here";

    // When & Then
    assertThrows(Exception.class, () -> {
      jwtUtil.extractClaims(invalidToken);
    });
  }

  @Test
  @DisplayName("應該拒絕使用錯誤密鑰簽名的token")
  void shouldRejectTokenWithWrongSecret() {
    // Given
    JwtUtil wrongSecretJwtUtil = new JwtUtil("wrongSecret123456789012345678901234567890", testExpiration);
    String userId = "usr_123456";
    String email = "test@example.com";
    String username = "testuser";
    String tokenWithWrongSecret = wrongSecretJwtUtil.generateToken(userId, email, username);

    // When & Then
    assertThrows(SignatureException.class, () -> {
      jwtUtil.extractClaims(tokenWithWrongSecret);
    });
  }

  @Test
  @DisplayName("應該檢測過期的token")
  void shouldDetectExpiredToken() {
    // Given - 創建一個立即過期的token
    JwtUtil shortExpirationJwtUtil = new JwtUtil(testSecret, -1); // 負數表示已過期
    String userId = "usr_123456";
    String email = "test@example.com";
    String username = "testuser";
    String expiredToken = shortExpirationJwtUtil.generateToken(userId, email, username);

    // When & Then
    assertThrows(ExpiredJwtException.class, () -> {
      jwtUtil.extractClaims(expiredToken);
    });
  }

  @Test
  @DisplayName("token應該包含正確的過期時間")
  void shouldHaveCorrectExpirationTime() {
    // Given
    String userId = "usr_123456";
    String email = "test@example.com";
    String username = "testuser";
    long beforeGeneration = System.currentTimeMillis();

    // When
    String token = jwtUtil.generateToken(userId, email, username);
    Claims claims = jwtUtil.extractClaims(token);
    long afterGeneration = System.currentTimeMillis();

    // Then
    long expectedExpiration = beforeGeneration + (testExpiration * 1000);
    long actualExpiration = claims.getExpiration().getTime();

    // 允許一些時間誤差（1秒）
    assertTrue(actualExpiration >= expectedExpiration - 1000);
    assertTrue(actualExpiration <= afterGeneration + (testExpiration * 1000));
  }
}