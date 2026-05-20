package com.smartmedical.utils;

import com.smartmedical.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class OssService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.url-prefix:http://localhost:8080/uploads}")
    private String urlPrefix;

    public String uploadImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String datePath = LocalDate.now().toString().replace("-", "/");
        String fileName = datePath + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

        Path uploadPath = Paths.get(uploadDir, fileName);
        try {
            Files.createDirectories(uploadPath.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, uploadPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }

        return urlPrefix + "/" + fileName;
    }
}
