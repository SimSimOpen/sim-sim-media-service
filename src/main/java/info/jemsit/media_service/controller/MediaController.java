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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMedia(@RequestParam(value = "property_id", required = false) Long property_id, @RequestPart("files") List<MultipartFile> files) {
        return ResponseEntity.ok(mediaService.uploadMedia(property_id, files));
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
