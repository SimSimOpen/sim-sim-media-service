package info.jemsit.media_service.service;

import java.time.Instant;

public record SessionResponseDTO (
        String sessionId,
        Long userId,
        Instant expiresAt
) {
}
