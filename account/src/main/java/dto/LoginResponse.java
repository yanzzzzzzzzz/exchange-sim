package dto;

import account.model.User;

public record LoginResponse(String accessToken, String refreshToken, String tokenType, int expiresIn, User user) {
}
