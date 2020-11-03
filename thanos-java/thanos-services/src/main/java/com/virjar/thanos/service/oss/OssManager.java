package com.virjar.thanos.service.oss;

import com.virjar.thanos.util.Environment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OssManager implements InitializingBean {

    @Resource
    private List<IOssService> iOssServiceList;

    private IOssService realOSSImpl;

    private static OssManager instance;

    public static void uploadFile(String ossRelativePath, File theUploadFile) {
        instance.realOSSImpl.uploadFile(ossRelativePath, theUploadFile);
    }

    public static void uploadFileAsync(String ossRelativePath, File theUploadFile, IOssService.UploadCallback uploadCallback) {
        instance.realOSSImpl.uploadFileAsync(ossRelativePath, theUploadFile, uploadCallback);
    }

    public static void downloadFile(String pathOrURL, File theDownloadFile) {
        instance.realOSSImpl.downloadFile(pathOrURL, theDownloadFile);
    }

    public static String genPublicAccessURL(String ossRelativePath) {
        return instance.realOSSImpl.genPublicAccessURL(ossRelativePath);
    }

    /**
     * mysql中，TEXT的最大长度，为65535，超过之后需要存储到OSS。我们卡阈值的时候提供128的buffer，预防极限情况下数据库编码和java中不一致两端长度测量标准不同意的问题
     */
    private static final int MAX_TEXT_LENGTH = (1 << 16) - 128;

    // 16k,小于16k的话，快速判定字符串长度。如果字符串长度小于16K，那么直接存储到数据库。16K代表极限情况UTF8Mb4，一个字符4个字节，达64k。64K为Mysql TEXT的容量
    private static final int K_16 = 1 << 14;

    private static final String KEY_OSS_STORE = "THANOS_OSS_STORE_KEY:";
    private static final String OSS_CACHE_PREFIX = "oss_blob";


    public static String transformToOssIfNeed(String key, String value) {
        if (value == null) {
            return null;
        }
        if (value.length() < K_16) {
            return value;
        }
        File tempFile = Environment.tempFile();
        try {
            String path = KEY_OSS_STORE + OSS_CACHE_PREFIX + "/" + key.trim();
            FileUtils.writeStringToFile(tempFile, value, StandardCharsets.UTF_8);
            uploadFile(path, tempFile);
            return path;
        } catch (IOException e) {
            log.error("create tmp filed failed", e);
            throw new RuntimeException("create tmp filed failed", e);
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                log.warn("can not remove temp file:{}", tempFile.getAbsolutePath());
            }
        }
    }

    public static String restoreOssContentIfHas(String encodeResult) {
        if (encodeResult == null) {
            return null;
        }
        if (!encodeResult.startsWith(KEY_OSS_STORE)) {
            return encodeResult;
        }

        String fileName = encodeResult.substring(KEY_OSS_STORE.length());
        File tempFile = Environment.tempFile();
        try {
            downloadFile(fileName, tempFile);
            return FileUtils.readFileToString(tempFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("unable to download oss file from oss services: ", e);
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                log.warn("can not remove temp file:{}", tempFile.getAbsolutePath());
            }
        }
        return encodeResult;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
        if (iOssServiceList == null) {
            throw new Exception("can not find any implementation for: " + IOssService.class.getName());
        }

        List<IOssService> enabledImplementations = iOssServiceList.stream().filter(IOssService::work).collect(Collectors.toList());
        if (enabledImplementations.isEmpty()) {
            throw new Exception("can not find any illegal implementation for: " + IOssService.class.getName() + " 请至少配置一种oss方案");
        }
        if (enabledImplementations.size() > 1) {
            log.warn("multi oss impl founds: {}", StringUtils.join(enabledImplementations.stream().map(it -> it.getClass().getName()).collect(Collectors.toList()), ","));
        }
        realOSSImpl = chooseImpl(enabledImplementations);
        log.info("use oss plan: {}", realOSSImpl.getClass().getName());
    }

    private IOssService chooseImpl(List<IOssService> enabledImplementations) {
        enabledImplementations.sort(Comparator.comparing(o -> o.getClass().getName()));
        return enabledImplementations.get(0);
    }
}
