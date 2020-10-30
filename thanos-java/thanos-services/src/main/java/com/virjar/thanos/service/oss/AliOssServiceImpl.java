package com.virjar.thanos.service.oss;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.internal.OSSUtils;
import com.aliyun.oss.model.AccessControlList;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.GetObjectRequest;
import com.virjar.thanos.service.RootConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.File;
import java.util.Date;

/**
 * @author virjar
 * @since 2020-10-29
 */
@Service
@Slf4j
public class AliOssServiceImpl extends IOssService implements Closeable {


    @Override
    public void uploadFile(String ossRelativePath, File theUploadFile) {
        if (check()) {
            ossClient.putObject(RootConfig.ALIOss.bucket, ossRelativePath, theUploadFile);
        }
    }

    @Override
    public void downloadFile(String pathOrURL, File theDownloadFile) {
        if (!check()) {
            return;
        }
        ossClient.getObject(new GetObjectRequest(RootConfig.ALIOss.bucket, trimURLToPath(pathOrURL)), theDownloadFile);

    }

    @Override
    public String genPublicAccessURL(String ossRelativePath) {
        if (!check()) {
            return null;
        }
        ossRelativePath = trimURLToPath(ossRelativePath);

        AccessControlList bucketAcl = ossClient.getBucketAcl(RootConfig.ALIOss.bucket);
        if (CannedAccessControlList.PublicRead == bucketAcl.getCannedACL()) {
            String url = ossClient.getEndpoint().toString();
            if (!url.endsWith("/")) {
                url += "/";
            }
            String resourcePath = OSSUtils.determineResourcePath(RootConfig.ALIOss.bucket, ossRelativePath, ossClient.getClientConfiguration().isSLDEnabled());
            url += resourcePath;
            return url;
        }
        return ossClient.generatePresignedUrl(RootConfig.ALIOss.bucket, ossRelativePath, new Date(new Date().getTime() + 1000 * 60 * 60 * 8)).toString();
    }

    @Override
    boolean work() {
        try {
            configureOssClient();
        } catch (Throwable ignore) {

        }
        return ossClient != null;
    }

    @Override
    String getBucket() {
        return RootConfig.ALIOss.bucket;
    }

    private boolean check() {
        if (ossClient == null) {
            log.error("the aliOssService can not work");
        }
        return ossClient != null;
    }


    private void configureOssClient() {
        OSSClient ossClient = new OSSClient(RootConfig.ALIOss.endpoint, RootConfig.ALIOss.accessKeyId, RootConfig.ALIOss.accessKeySecret);
        if (ossClient.doesBucketExist(RootConfig.ALIOss.bucket)) {
            this.ossClient = ossClient;
        } else {
            log.warn("the bucket:{} not exist", RootConfig.ALIOss.bucket);
        }
    }

    private OSSClient ossClient = null;

    @Override
    public void close() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
