package com.virjar.thanos.service.oss;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
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

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
        if (iOssServiceList == null) {
            throw new Exception("can not find any implementation for: " + IOssService.class.getName());
        }

        List<IOssService> enabledImplementations = iOssServiceList.stream().filter(IOssService::work).collect(Collectors.toList());
        if (enabledImplementations.isEmpty()) {
            throw new Exception("can not find any illegal implementation for: " + IOssService.class.getName());
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
