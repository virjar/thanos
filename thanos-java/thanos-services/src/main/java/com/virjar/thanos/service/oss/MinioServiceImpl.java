package com.virjar.thanos.service.oss;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;


/**
 * https://github.com/minio/minio<br>
 * Minio是开源的，自建的oss存储，如果你不想买阿里云、公司也没有搭建hbase，那么可以尝试搞一个minio作为oss存储
 */
@Slf4j
@Service
public class MinioServiceImpl extends IOssService {

    @Value("${oss.minio.bucket}")
    private String endpoint;

    @Value("${oss.minio.accessKey}")
    private String accessKeyId;

    @Value("${oss.minio.secretKey}")
    private String accessKeySecret;

    @Value("${oss.minio.bucket}")
    private String bucket;

    @Override
    public void uploadFile(String ossRelativePath, File theUploadFile) {
        if (!check()) {
            return;
        }
        try {
            minioClient.putObject(bucket, ossRelativePath, theUploadFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("failed to upload file:{}", theUploadFile);
            throw new IllegalStateException("ailed to upload file", e);
        }
    }

    @Override
    public void downloadFile(String pathOrURL, File theDownloadFile) {
        if (!check()) {
            return;
        }
        try {
            try (InputStream inputStream = minioClient.getObject(bucket, trimURLToPath(pathOrURL))) {
                FileUtils.copyInputStreamToFile(inputStream, theDownloadFile);
            }
        } catch (Exception e) {
            log.error("failed to download file:{}", theDownloadFile);
            throw new IllegalStateException("ailed to upload file", e);
        }
    }

    @Override
    public String genPublicAccessURL(String ossRelativePath) {
        //TODO 暂时都产生有过期时间的URL
        try {
            return minioClient.presignedGetObject(bucket, trimURLToPath(ossRelativePath));
        } catch (Exception e) {
            log.error("failed to genPublicAccessURL ossRelativePath:{}", ossRelativePath);
            throw new IllegalStateException("ailed to upload file", e);
        }
    }

    @Override
    boolean work() {
        try {
            configureOssClient();
        } catch (Throwable ignore) {

        }
        return minioClient != null;
    }

    @Override
    String getBucket() {
        return bucket;
    }

    private MinioClient minioClient;

    private boolean check() {
        if (minioClient == null) {
            log.error("the aliOssService can not work");
        }
        return minioClient != null;
    }


    private void configureOssClient() throws Exception {
        MinioClient minioClient = new MinioClient(endpoint, accessKeyId, accessKeySecret);
        if (minioClient.bucketExists(bucket)) {
            this.minioClient = minioClient;
        } else {
            log.warn("the bucket:{} not exist", bucket);
        }
    }
}
