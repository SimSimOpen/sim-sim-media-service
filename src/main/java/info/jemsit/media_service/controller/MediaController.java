package info.jemsit.media_service.controller;

import info.jemsit.media_service.service.MediaService;
import info.jemsit.media_service.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class MediaController {

    private final MediaService mediaService;
    private final SessionService sessionService;

    @PostMapping(value = "/upload-product-media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMedia(@RequestParam(value = "property_id", required = false) Long property_id, @RequestPart("files") List<MultipartFile> files) {
        return ResponseEntity.ok(mediaService.uploadMedia(property_id, files));
    }

    @PostMapping(value = "/upload-user-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadUserAvatar(@RequestParam("user_id") Long user_id,  @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(mediaService.uploadUserAvatar(user_id,file));
    }

    @DeleteMapping("/delete/media")
    public ResponseEntity<?> deleteMedia(@RequestParam("media-url") String mediaUrl) {
        mediaService.deleteMedia(mediaUrl);
        return ResponseEntity.ok("Media deleted successfully");
    }

    @PostMapping("/session")
    public ResponseEntity<?> createUploadSession() {
        return ResponseEntity.ok(sessionService.createUploadSession());
    }

    @GetMapping("/session/{sessionId}/status")
    public void getSession(@PathVariable String sessionId) {
        sessionService.getSession(sessionId);
    }
}
