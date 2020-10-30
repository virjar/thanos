package com.virjar.thanos.service.engine;

import com.virjar.thanos.service.oss.OssManager;
import com.virjar.thanos.util.Environment;
import com.virjar.thanos.api.util.Md5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class OssJarFileManager {
    //private static final String SUFFIX_META = ".meta";
    public static final String SUFFIX_JAR = ".jar";

    public void downloadJar(String md5, String crawler, String pathOrURL) throws IOException {
        File cachePath = Environment.wrapperJar(crawler, md5);
        if (cachePath.exists()) {
            //download already
            return;
        }
        File target = new File(Environment.wrapperJarDir(crawler), md5 + ".download");
        OssManager.downloadFile(pathOrURL, target);

        if (!target.exists() || !target.canRead()) {
            throw new IOException("file download failed: " + target.getAbsolutePath() + " url:" + pathOrURL);
        }
        if (!Md5Utils.md5(target).equals(md5)) {
            throw new IOException("md5 not match..");
        }

        try {
            FileUtils.moveFile(target, cachePath);
        } finally {
            if (!Md5Utils.md5(cachePath).equals(md5)) {
                FileUtils.forceDelete(cachePath);
            }
        }

    }


    public void cacheJar(String md5, String crawler, File targetFile) throws IOException {
        if (!targetFile.exists() || !targetFile.canRead()) {
            throw new IllegalStateException("can not access file:" + targetFile.getAbsolutePath());
        }
        if (!Md5Utils.md5(targetFile).equals(md5)) {
            throw new IllegalStateException("the md5 not match for file:" + targetFile.getAbsolutePath());
        }
        File cachePath = Environment.wrapperJar(crawler, md5);
        if (cachePath.exists() && Md5Utils.md5(cachePath).equals(md5)) {
            return;
        }

        File tempFile = new File(Environment.wrapperJarDir(crawler), md5 + SUFFIX_JAR + ".tmp");
        FileUtils.copyFile(targetFile, tempFile);
        try {
            FileUtils.moveFile(tempFile, cachePath);
        } finally {
            if (!Md5Utils.md5(cachePath).equals(md5)) {
                FileUtils.forceDelete(cachePath);
            }
        }
    }
//
//
//    @Data
//    private static class OSSFileMeta {
//        private String path;
//        private long create;
//        private long lastUse;
//        private String md5;
//    }

}
