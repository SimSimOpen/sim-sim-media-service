package info.jemsit.media_service.data.dao.impl;

import info.jemsit.media_service.data.dao.SessionDAO;
import info.jemsit.media_service.data.model.Session;
import info.jemsit.media_service.data.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionDAOImpl implements SessionDAO {
    private final SessionRepository sessionRepository;
    @Override
    public Session save(Session session) {
        log.info("Saving session");
        return sessionRepository.save(session);
    }

    @Override
    public Optional<Session> getBySessionId(String sessionId) {
        log.info("Getting session by sessionId: {}", sessionId);
        return sessionRepository.findBySessionId(sessionId);
    }
}
