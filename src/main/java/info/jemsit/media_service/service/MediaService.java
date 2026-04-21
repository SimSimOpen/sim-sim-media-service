package info.jemsit.media_service.service;

import info.jemsit.common.dto.response.product.propeprty.PropertyResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService {
    Long  uploadMedia(Long id, List<MultipartFile> files);
    void deleteMedia(String mediaUrl);
    Long uploadUserAvatar(Long userId, MultipartFile file);
}
