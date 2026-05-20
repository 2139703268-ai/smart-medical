package com.smartmedical.controller;

import com.smartmedical.common.BusinessException;
import com.smartmedical.common.Result;
import com.smartmedical.utils.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final OssService ossService;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }
        String url = ossService.uploadImage(file);
        return Result.success(Map.of("url", url));
    }
}
