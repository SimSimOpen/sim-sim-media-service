package info.jemsit.media_service.service;

public interface SessionService {
    SessionResponseDTO createUploadSession();
    void getSession(String sessionID);
}
