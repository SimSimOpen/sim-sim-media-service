package info.jemsit.media_service.service.impl;

import info.jemsit.common.UserContext;
import info.jemsit.common.clients.property.ProductServiceClient;
import info.jemsit.common.dto.request.product.property.AddPropertyImagesRequestDTO;
import info.jemsit.media_service.data.dao.SessionDAO;
import info.jemsit.media_service.service.AsyncMediaService;
import info.jemsit.media_service.service.FileData;
import info.jemsit.media_service.service.ImageProcessingService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncMediaServiceImpl implements AsyncMediaService {

    private final MinioClient minioClient;
    private final ProductServiceClient productServiceClient;

    private final ImageProcessingService imageProcessingService;
    private final SessionDAO sessionDAO;

    private final String bucketName = "real-estate-media";


    @Override
    @Async
    public void asyncMediaUpload(Long propertyId, List<FileData> files, String token){
        UserContext.setUserToken(token); // restore on async thread
        try(var executor = Executors.newVirtualThreadPerTaskExecutor()){
            List<String> urls = files.stream()
                    .map(file-> executor.submit(()->processAndUpload(file.bytes(), file.originalFileName(), propertyId)))
                    .toList()
                    .stream()
                    .map(future -> {
                        try{return future.get();}
                        catch (Exception e) {
                            log.error("Error processing and uploading file: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
            productServiceClient.addPropertyImage(new AddPropertyImagesRequestDTO(propertyId, urls));
        }finally {
            UserContext.clear(); // clear context to prevent memory leaks
        }
    }

    private String processAndUpload(byte[] bytes, String filename, Long id) throws Exception {
        byte[] processedBytes = imageProcessingService.processImageWithWaterMark(bytes, filename);
        String objKey = "properties/" + id + "/images/" + UUID.randomUUID() + ".webp";

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objKey)
                        .contentType("image/webp")
                        .stream(new ByteArrayInputStream(processedBytes), processedBytes.length, -1)
                        .build()
        );

        return "/" + bucketName + "/" + objKey;
    }
}
