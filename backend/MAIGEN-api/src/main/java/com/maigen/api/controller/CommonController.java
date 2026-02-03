package com.maigen.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.maigen.common.core.exception.CustomException;

import com.maigen.common.miniIO.util.MiniIOUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
@Tag(name = "公共接口", description = "文件上传等公共功能")
@Slf4j
public class CommonController {

    private final MiniIOUtil miniIOUtil;

    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    @SaCheckLogin
    public SaResult upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException("文件不能为空", 404);
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = "avatars/" + UUID.randomUUID().toString() + extension;
            String bucketName = "maigen";

            miniIOUtil.uploadStream(
                    bucketName,
                    objectName,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );

            // 返回预签名URL或MinIO公开访问URL
            // 这里我们简单返回一个可访问的URL（假设MinIO配置了公开访问或通过我们的接口获取）
            // 生产环境下建议通过预签名URL或Nginx转发
            String url = miniIOUtil.getPresignedUrl(bucketName, objectName);
            return SaResult.data(url);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new CustomException("文件上传失败: " + e.getMessage(), 404);
        }
    }
}
