package info.jemsit.media_service.service.impl;

import info.jemsit.common.clients.property.ProductServiceClient;
import info.jemsit.common.dto.request.product.property.AddPropertyImagesRequestDTO;
import info.jemsit.common.dto.response.product.propeprty.PropertyResponseDTO;
import info.jemsit.common.exceptions.UserException;
import info.jemsit.media_service.data.dao.SessionDAO;
import info.jemsit.media_service.service.ImageProcessingService;
import info.jemsit.media_service.service.MediaService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MinioClient minioClient;

    private final ProductServiceClient productServiceClient;
    private final ImageProcessingService imageProcessingService;

    private final SessionDAO sessionDAO;

    private final String bucketName = "real-estate-media";

    @Override
    public PropertyResponseDTO uploadMedia(Long id, List<MultipartFile> files) {

        if(id == null) id = productServiceClient.createDraftProperty().id();

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                byte[] processedBytes = imageProcessingService.processImageWithWaterMark(file);
                String objKey = "properties/" + id + "/images/" + UUID.randomUUID() + ".webp";

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objKey)
                                .contentType("image/webp")
                                .stream(new ByteArrayInputStream(processedBytes), processedBytes.length, -1)
                                .build()
                );
                urls.add("/" + bucketName + "/" + objKey);
            }catch (UserException e){
                log.error("Image processing failed: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Error uploading file to MinIO: {}", e.getMessage());
                throw new UserException("Failed to upload image. Please try again.");
            }
        }
        return productServiceClient.addPropertyImage(new AddPropertyImagesRequestDTO(id, urls));
    }

    @Override
    public void deleteMedia(String mediaUrl) {
        var media = mediaUrl.split("/");
        System.out.println(media[1]);
        System.out.println(mediaUrl.substring(media[1].length() + 1));
        try{
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

    private String getExt(String name) {
        if (name == null) return "";
        int lastIndex = name.lastIndexOf(".");
        return lastIndex >= 0 ? name.substring(lastIndex) : "";
    }
}
