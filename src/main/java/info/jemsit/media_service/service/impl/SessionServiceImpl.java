package info.jemsit.media_service.service.impl;

import info.jemsit.common.clients.auth.AuthServiceClient;
import info.jemsit.common.exceptions.UserException;
import info.jemsit.media_service.data.dao.SessionDAO;
import info.jemsit.media_service.data.model.Session;
import info.jemsit.media_service.mapper.SessionMapper;
import info.jemsit.media_service.service.SessionResponseDTO;
import info.jemsit.media_service.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

    private final SessionDAO sessionDAO;
    private final AuthServiceClient authServClient;
    private final SessionMapper sessionMapper;

    @Override
    public SessionResponseDTO createUploadSession() {
        Session session = new Session();
        session.setSessionId(UUID.randomUUID().toString());
        var user = authServClient.getUserDetails();
        session.setUserId(user.id());
        session.setExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));
        session = sessionDAO.save(session);
        return sessionMapper.toDto(session);
    }

    @Override
    public void getSession(String sessionID) {
         var session = sessionDAO.getBySessionId(sessionID)
                .orElseThrow(() -> new UserException("Session not found"));
         if(session.getExpiresAt().isBefore(Instant.now())) {
             throw new UserException("Session expired");
         }
    }
}
