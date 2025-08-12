package dto;

public record LoginResponse(
    String accessToken,
    String tokenType,
    int expiresIn,
    UserInfo user) {
}
