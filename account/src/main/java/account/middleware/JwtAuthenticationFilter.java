package account.middleware;

import account.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String userId;

    // 如果沒有Authorization header或不是Bearer token，繼續執行
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    jwt = authHeader.substring(7);

    try {
      userId = jwtUtil.extractUserId(jwt);

      // 如果token有效且用戶未認證
      if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        // 驗證token
        if (jwtUtil.validateToken(jwt, userId)) {
          // 創建認證對象
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userId,
              null,
              new ArrayList<>());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          // 設置到SecurityContext
          SecurityContextHolder.getContext().setAuthentication(authToken);

          // 將用戶資訊添加到request attributes
          request.setAttribute("userId", userId);
          request.setAttribute("email", jwtUtil.extractEmail(jwt));
          request.setAttribute("username", jwtUtil.extractUsername(jwt));
        }
      }
    } catch (Exception e) {
      // Token無效，不設置認證
      logger.debug("JWT token validation failed: " + e.getMessage());
    }

    filterChain.doFilter(request, response);
  }
}