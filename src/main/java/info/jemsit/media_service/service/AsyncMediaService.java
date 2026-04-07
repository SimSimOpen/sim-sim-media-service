package info.jemsit.media_service.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AsyncMediaService {
    void asyncMediaUpload(Long id, List<FileData> files, String token);
}
