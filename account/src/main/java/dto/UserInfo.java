package dto;

import java.time.Instant;

public record UserInfo(
    String id,
    String email,
    String username,
    Instant createdAt,
    Instant updatedAt) {
}