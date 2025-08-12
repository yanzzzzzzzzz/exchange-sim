package account.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtUtil {

  private final SecretKey secretKey;
  private final long expirationTime;

  public JwtUtil(@Value("${jwt.secret:mySecretKeyForJWTTokenGenerationThatIsLongEnough}") String secret,
      @Value("${jwt.expiration:3600}") long expirationTime) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.expirationTime = expirationTime;
  }

  public String generateToken(String userId, String email, String username) {
    Instant now = Instant.now();
    Instant expiration = now.plus(expirationTime, ChronoUnit.SECONDS);

    return Jwts.builder()
        .subject(userId)
        .claim("email", email)
        .claim("username", username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiration))
        .signWith(secretKey)
        .compact();
  }

  public Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String extractUserId(String token) {
    return extractClaims(token).getSubject();
  }

  public String extractEmail(String token) {
    return extractClaims(token).get("email", String.class);
  }

  public String extractUsername(String token) {
    return extractClaims(token).get("username", String.class);
  }

  public boolean isTokenExpired(String token) {
    return extractClaims(token).getExpiration().before(new Date());
  }

  public boolean validateToken(String token, String userId) {
    return extractUserId(token).equals(userId) && !isTokenExpired(token);
  }
}