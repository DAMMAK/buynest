package dev.dammak.productservice.util;


import dev.dammak.productservice.config.FileUploadConfig;
import dev.dammak.productservice.exception.ProductException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileUtil {

    private final FileUploadConfig fileUploadConfig;

    public List<String> uploadProductImages(Long productId, List<MultipartFile> files) {
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFile(file);
            String imageUrl = uploadSingleImage(productId, file);
            imageUrls.add(imageUrl);
        }

        return imageUrls;
    }

    private String uploadSingleImage(Long productId, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            Path uploadPath = Paths.get(fileUploadConfig.getUploadDir(), "product-" + productId);
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            String imageUrl = "/images/products/product-" + productId + "/" + newFilename;
            log.info("Uploaded image: {}", imageUrl);

            return imageUrl;
        } catch (IOException e) {
            throw new ProductException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ProductException("File is empty");
        }

        if (file.getSize() > fileUploadConfig.getMaxFileSize()) {
            throw new ProductException("File size exceeds maximum allowed size");
        }

        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(fileUploadConfig.getAllowedTypes()).contains(contentType)) {
            throw new ProductException("Invalid file type. Allowed types: " +
                    Arrays.toString(fileUploadConfig.getAllowedTypes()));
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}