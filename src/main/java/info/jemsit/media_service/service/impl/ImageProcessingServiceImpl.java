package info.jemsit.media_service.service.impl;

import info.jemsit.common.exceptions.UserException;
import info.jemsit.media_service.service.ImageProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class ImageProcessingServiceImpl implements ImageProcessingService {
    @Override
    public byte[] processImage(MultipartFile file) throws Exception {

        Path inputPath = Files.createTempFile("upload-", getExt(file.getOriginalFilename()));
        Path outputPath = Files.createTempFile("processed-", getExt(file.getOriginalFilename()));
        try {
            Files.write(inputPath, file.getBytes());
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputPath.toString(),
                    "-vf", "scale=1920:-1",
                    "-quality", "82",
                    "-y",
                    outputPath.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            //log ffmpeg output for debugging
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("ffmpeg failed with exit code {}: {}", exitCode, output);
                throw new UserException("Image processing failed: " + output);
            }
            return Files.readAllBytes(outputPath);

        } finally {
            Files.deleteIfExists(inputPath);
            Files.deleteIfExists(outputPath);
        }

    }

    private String getExt(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }
}
