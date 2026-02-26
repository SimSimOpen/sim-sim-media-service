package info.jemsit.media_service.data.dao;

import info.jemsit.media_service.data.model.Session;

import java.util.Optional;

public interface SessionDAO {
    Session save(Session session);
    Optional<Session> getBySessionId(String sessionId);
}
