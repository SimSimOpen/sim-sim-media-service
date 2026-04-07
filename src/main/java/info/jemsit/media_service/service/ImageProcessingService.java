package info.jemsit.media_service.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageProcessingService {

    byte[] processImage(MultipartFile file) throws Exception;
    byte[] processImageWithWaterMark(byte[] bytes, String filename) throws Exception;
}
