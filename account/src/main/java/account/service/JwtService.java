package account.service;

import account.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

  @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationThatShouldBeLongEnough}")
  private String secretKey;

  @Value("${jwt.expiration:3600}")
  private long jwtExpiration;

  public String generateToken(User user) {
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("username", user.getUsername());
    extraClaims.put("email", user.getEmail());

    return generateToken(extraClaims, user.getId());
  }

  public String generateToken(Map<String, Object> extraClaims, String subject) {
    return Jwts.builder()
        .claims(extraClaims)
        .subject(subject)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000))
        .signWith(getSignInKey())
        .compact();
  }

  public boolean isTokenValid(String token, String userId) {
    final String extractedUserId = extractUserId(token);
    return (extractedUserId.equals(userId)) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public String extractUserId(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public String extractUsername(String token) {
    return extractClaim(token, claims -> claims.get("username", String.class));
  }

  public String extractEmail(String token) {
    return extractClaim(token, claims -> claims.get("email", String.class));
  }

  public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSignInKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey getSignInKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes());
  }
}