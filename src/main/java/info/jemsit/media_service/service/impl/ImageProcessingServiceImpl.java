package info.jemsit.media_service.service.impl;

import info.jemsit.common.exceptions.UserException;
import info.jemsit.media_service.service.ImageProcessingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class ImageProcessingServiceImpl implements ImageProcessingService {


    private Path logoTempPath;

    @PostConstruct
    public void init() throws Exception {
        // ✅ extract logo from jar to temp file once on startup
        ClassPathResource logo = new ClassPathResource("SimSim-Logo.png");
        logoTempPath = Files.createTempFile("simsim-logo-", ".png");
        Files.copy(logo.getInputStream(), logoTempPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Logo extracted to: {}", logoTempPath);
    }

    @PreDestroy
    public void cleanup() throws Exception {
        // ✅ clean up on shutdown
        Files.deleteIfExists(logoTempPath);
    }

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

    @Override
    public byte[] processImageWithWaterMark(MultipartFile file) throws Exception {
        Path inputPath = Files.createTempFile("upload-", getExt(file.getOriginalFilename()));
        Path outputPath = Files.createTempFile("processed-", getExt(file.getOriginalFilename()));
        try {
            Files.write(inputPath, file.getBytes());
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputPath.toString(),
                    "-i", logoTempPath.toString(),      // watermark path
                    "-filter_complex",
                    "[1:v]scale=150:-1[logo];" +                              // ✅ resize logo to 150px wide
                            "[0:v]scale=1920:-1,eq=brightness=0.05:contrast=1.1:saturation=1.2[img];" +
                            "[img][logo]overlay=W-w-20:H-h-20",
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
                log.error("ffmpeg failed in watermark inputting with exit code {}: {}", exitCode, output);
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
