package account.controller;

import account.exception.ConflictException;
import account.exception.ValidationException;
import account.service.AccountService;
import dto.ErrorResponse;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.RegisterRequest;
import dto.RegisterResponse;
import dto.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    try {
      RegisterResponse response = accountService.register(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (ValidationException e) {
      ErrorResponse errorResponse = new ErrorResponse(
          e.getError(),
          e.getMessage(),
          e.getDetails());
      return ResponseEntity.badRequest().body(errorResponse);
    } catch (ConflictException e) {
      ErrorResponse errorResponse = new ErrorResponse(
          e.getError(),
          e.getMessage(),
          null);
      return ResponseEntity.status(409).body(errorResponse);
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    try {
      LoginResponse response = accountService.login(request);
      return ResponseEntity.ok(response);
    } catch (ValidationException e) {
      ErrorResponse errorResponse = new ErrorResponse(
          e.getError(),
          e.getMessage(),
          e.getDetails());
      return ResponseEntity.badRequest().body(errorResponse);
    } catch (Exception e) {
      ErrorResponse errorResponse = new ErrorResponse("invalid_credentials", "email or password is incorrect", null);
      return ResponseEntity.status(401).body(errorResponse);
    }
  }

  @GetMapping("/me")
  public ResponseEntity<?> getCurrentUser(Authentication authentication, HttpServletRequest request) {
    try {
      String userId = (String) authentication.getPrincipal();

      UserInfo userInfo = accountService.getUserById(userId);

      return ResponseEntity.ok(userInfo);
    } catch (Exception e) {
      ErrorResponse errorResponse = new ErrorResponse("user_not_found", "User not found", null);
      return ResponseEntity.status(404).body(errorResponse);
    }
  }

  @GetMapping("/users/{id}")
  public ResponseEntity<?> getUser(@PathVariable("id") String id) {
    try {
      UserInfo userInfo = accountService.getUserById(id);
      return ResponseEntity.ok(userInfo);
    } catch (Exception e) {
      ErrorResponse errorResponse = new ErrorResponse("user_not_found", "User not found", null);
      return ResponseEntity.status(404).body(errorResponse);
    }
  }
}
