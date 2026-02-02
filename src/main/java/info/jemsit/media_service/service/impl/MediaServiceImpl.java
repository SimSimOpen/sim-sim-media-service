package info.jemsit.media_service.service.impl;

import info.jemsit.common.clients.property.ProductServiceClient;
import info.jemsit.common.dto.response.product.propeprty.PropertyResponseDTO;
import info.jemsit.common.exceptions.UserException;
import info.jemsit.media_service.service.MediaService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MinioClient minioClient;

    private final ProductServiceClient productServiceClient;

    private final String bucketName = "real-estate-media";

    @Override
    public PropertyResponseDTO uploadMedia(Long id, List<MultipartFile> files) {

        if(id == null) id = productServiceClient.createDraftProperty().id();

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {

            String fileName = getExt(file.getOriginalFilename());
            String objKey = "properties/" + id + "/images/" + UUID.randomUUID() + fileName;

            try {

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objKey)
                                .contentType(file.getContentType())
                                .stream(file.getInputStream(), file.getSize(), -1)
                                .build()
                );
                urls.add("/" + bucketName + "/" + objKey);
            } catch (Exception e) {
                log.error("Error uploading file to MinIO: {}", e.getMessage());
                throw new UserException("Failed to upload image. Please try again.");
            }
        }
        return productServiceClient.addPropertyImage(id, urls);
    }

    private String getExt(String name) {
        if (name == null) return "";
        int lastIndex = name.lastIndexOf(".");
        return lastIndex >= 0 ? name.substring(lastIndex) : "";
    }
}
