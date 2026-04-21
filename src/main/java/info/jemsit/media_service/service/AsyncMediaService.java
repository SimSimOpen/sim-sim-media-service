package info.jemsit.media_service.service;

import java.util.List;

public interface AsyncMediaService {
    void asyncProductMediaUpload(Long propertyId, List<FileData> files, String token);

    void asyncUserAvatarUpload(Long userId, FileData file);
}
