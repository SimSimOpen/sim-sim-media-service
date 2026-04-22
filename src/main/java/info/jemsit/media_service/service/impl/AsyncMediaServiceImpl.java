package info.jemsit.media_service.service.impl;

import info.jemsit.common.UserContext;
import info.jemsit.common.clients.property.ProductServiceClient;
import info.jemsit.common.dto.message.UserAvatarUpdated;
import info.jemsit.common.dto.request.product.property.AddPropertyImagesRequestDTO;
import info.jemsit.media_service.service.AsyncMediaService;
import info.jemsit.media_service.service.FileData;
import info.jemsit.media_service.service.ImageProcessingService;
import info.jemsit.media_service.service.RabbitMQService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncMediaServiceImpl implements AsyncMediaService {

    private final MinioClient minioClient;
    private final ProductServiceClient productServiceClient;
    private final ImageProcessingService imageProcessingService;
    private final RabbitMQService rabbitMQService;

    private final Semaphore ffmpegSemaphore; // injected from Config, controls max concurrent FFmpeg processes
    private final String PROPERTIES_BASE_PATH = "properties/";
    private final  String USER_AVATARS_BASE_PATH = "user-avatars/";
    private final String productBucketName = "real-estate-media";
    private final String userBucketName = "user-media";


    @Override
    @Async
    public void asyncProductMediaUpload(Long propertyId, List<FileData> files, String token) {
        UserContext.setUserToken(token);
        try {
            List<String> urls = processFilesWithBoundedConcurrency(propertyId, files);

            if (urls.isEmpty()) {
                log.error("All file uploads failed for property {}", propertyId);
                return;
            }

            if (urls.size() < files.size()) {
                log.warn("Partial upload for property {}: {}/{} files succeeded",
                        propertyId, urls.size(), files.size());
            }

            productServiceClient.addPropertyImage(
                    new AddPropertyImagesRequestDTO(propertyId, urls)
            );

        } catch (Exception e) {
            log.error("Fatal error during media upload for property {}: {}",
                    propertyId, e.getMessage(), e);
        } finally {
            UserContext.clear();
        }
    }

    private List<String> processFilesWithBoundedConcurrency(Long propertyId, List<FileData> files) {
        // CompletableFuture with virtual threads — no blocking .get() in a loop
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<String>> futures = files.stream()
                    .map(file -> CompletableFuture.supplyAsync(
                            () -> processWithSemaphore(file, propertyId), executor))
                    .toList();

            return futures.stream()
                    .map(f -> f.exceptionally(ex -> {
                        log.error("Upload task failed: {}", ex.getMessage());
                        return null;
                    }))
                    .map(CompletableFuture::join) // safe — exceptions already handled above
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    private String processWithSemaphore(FileData file, Long propertyId) {
        try {
            ffmpegSemaphore.acquire(); // blocks virtual thread (not a carrier thread) ✅
            try {
                return processAndUpload(
                        PROPERTIES_BASE_PATH,
                        productBucketName,
                        file.bytes(),
                        file.originalFileName(),
                        propertyId
                );
            } finally {
                ffmpegSemaphore.release();
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted waiting for FFmpeg semaphore", e);
        }
    }

    @Override
    public void asyncUserAvatarUpload(Long userId, FileData file) {
            try {
                String url = processAndUpload(USER_AVATARS_BASE_PATH, userBucketName, file.bytes(), file.originalFileName(), userId);
                rabbitMQService.sendMessageToRabbitMQ(new UserAvatarUpdated(userId.toString(), url));
            } catch (Exception e) {
                log.error("Error processing and uploading user avatar: {}", e.getMessage());
            }
    }

    private String processAndUpload(String basePath, String bucketName,   byte[] bytes, String filename, Long id) throws Exception {
        byte[] processedBytes = imageProcessingService.processImageWithWaterMark(bytes, filename);
        String objKey = basePath + id + "/images/" + UUID.randomUUID() + ".webp";

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
