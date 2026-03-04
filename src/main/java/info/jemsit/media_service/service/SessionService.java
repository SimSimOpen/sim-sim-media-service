package info.jemsit.media_service.service;

import info.jemsit.common.dto.response.media.SessionResponseDTO;

public interface SessionService {
    SessionResponseDTO createUploadSession();
    void getSession(String sessionID);
}
