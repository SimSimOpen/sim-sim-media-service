package info.jemsit.media_service.service.impl;

import info.jemsit.common.UserContext;
import info.jemsit.common.clients.property.ProductServiceClient;
import info.jemsit.common.exceptions.UserException;
import info.jemsit.media_service.service.AsyncMediaService;
import info.jemsit.media_service.service.FileData;
import info.jemsit.media_service.service.MediaService;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MinioClient minioClient;
    private final ProductServiceClient productServiceClient;

    private final AsyncMediaService asyncMediaService;

    @Override
    public Long uploadMedia(Long id, List<MultipartFile> files) {

        if (id == null) id = productServiceClient.createDraftProperty().id();

        String token = UserContext.getUserToken(); // capture on request thread ✅
        final Long propertyId = id;

        List<FileData> fileDataList = files.stream()
                .map(file -> {
                    try {return new FileData(file.getBytes(), file.getOriginalFilename());}
                    catch (Exception e) {
                        log.error("Error reading file bytes: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        asyncMediaService.asyncProductMediaUpload(propertyId, fileDataList, token);

        return propertyId;
    }


    @Override
    public void deleteMedia(String mediaUrl) {
        var media = mediaUrl.split("/");
        System.out.println(media[1]);
        System.out.println(mediaUrl.substring(media[1].length() + 1));
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(media[1])
                            .object(mediaUrl.substring(media[1].length() + 1))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", e.getMessage());
            throw new UserException("Failed to delete image. Please try again.");
        }
    }

    @Override
    public Long uploadUserAvatar(Long userId, MultipartFile file) {
        try {
            asyncMediaService.asyncUserAvatarUpload(userId, new FileData(file.getBytes(), file.getOriginalFilename()));
            return userId;
        } catch (Exception e) {
            log.error("Error reading user avatar file bytes: {}", e.getMessage());
            throw new UserException("Failed to upload avatar. Please try again.");
        }
    }
}
