package com.maigen.common.miniIO.util;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import java.io.InputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * MinIO工具类
 */
public class MiniIOUtil {

    private final MinioClient minioClient;

    public MiniIOUtil(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 上传文件
     */
    public void uploadFile(String bucketName, String objectName, InputStream inputStream, long size, String contentType) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, size, -1)
                .contentType(contentType)
                .build()
        );
    }

    /**
     * 获取文件
     */
    public InputStream getFile(String bucketName, String objectName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build()
        );
    }

    /**
     * 删除文件
     */
    public void deleteFile(String bucketName, String objectName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build()
        );
    }
}