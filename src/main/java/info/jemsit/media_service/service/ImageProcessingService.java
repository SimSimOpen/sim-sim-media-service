package info.jemsit.media_service.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageProcessingService {

    byte[] processImage(MultipartFile file) throws Exception;
    byte[] processImageWithWaterMark(MultipartFile file) throws Exception;
}
