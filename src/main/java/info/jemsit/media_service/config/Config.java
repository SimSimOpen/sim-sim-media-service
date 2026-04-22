package info.jemsit.media_service.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Semaphore;

@Configuration
public class Config {

    @Value("${minio.url}")
    private String url;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }


    // Max concurrent FFmpeg processes — tune based on your container memory
    // 3 × ~100MB = ~300MB for FFmpeg, safe within 1400M limit
    @Bean
    public Semaphore ffmpegSemaphore() {
        return new Semaphore(3);
    }

}
