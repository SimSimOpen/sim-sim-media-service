package info.jemsit.media_service.controller;

import info.jemsit.media_service.service.MediaService;
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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMedia(@RequestParam(value = "property_id", required = false) Long property_id, @RequestPart("files") List<MultipartFile> files) {
        return ResponseEntity.ok(mediaService.uploadMedia(property_id, files));
    }
}
