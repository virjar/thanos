package com.virjar.thanos.service.oss;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author virjar
 * @since 2020-10-29
 */
@Slf4j
public abstract class IOssService {

    /**
     * 上传一个文件到oss
     *
     * @param ossRelativePath 在oss上的相对路径，不包括bucket
     * @param theUploadFile   将要被上传的文件
     */
    public abstract void uploadFile(String ossRelativePath, File theUploadFile);

    synchronized void uploadFileAsync(String ossRelativePath, File theUploadFile, UploadCallback uploadCallback) {
        if (!asyncTaskThread.isAlive()) {
            asyncTaskThread.start();
        }
        if (StringUtils.isBlank(ossRelativePath)) {
            throw new IllegalStateException("ossRelativePath can not be empty");
        }
        if (theUploadFile == null) {
            throw new IllegalStateException("theUploadFile can not be empty");
        }
        if (!theUploadFile.isFile() || !theUploadFile.canRead()) {
            throw new IllegalStateException("can not access file: " + theUploadFile.getAbsolutePath());
        }
        if (uploadCallback == null) {
            uploadCallback = (status, error) -> {/*do nothing*/};
        }
        queue.add(new AsyncUploadTask(ossRelativePath, theUploadFile, uploadCallback));
    }

    /**
     * 从oss下载一个文件
     *
     * @param pathOrURL       在oss上的相对路径(不包括bucket)、或者完整的可以被访问的http url
     * @param theDownloadFile 本地文件系统的下载地址
     */
    public abstract void downloadFile(String pathOrURL, File theDownloadFile);


    /**
     * 构造一个可以公开访问的url，如果bucket是公开的；
     * 如果这个bucket不是公开的，那么生产一个有效期为8个小时的http/https链接
     *
     * @param ossRelativePath 在oss上的相对路径，不包括bucket
     * @return http/https的url地址
     */
    public abstract String genPublicAccessURL(String ossRelativePath);

    /**
     * 用户一般只需要配置一种oss的实现即可
     *
     * @return 是否生效
     */
    abstract boolean work();

    /**
     * oss一定存在bucket
     *
     * @return bucket名称
     */
    abstract String getBucket();

    public interface UploadCallback {
        void onUploadFinished(boolean status, Throwable error);
    }

    String trimURLToPath(String pathOrURL) {
        if (pathOrURL.startsWith("http://") || pathOrURL.startsWith("https://")) {
            try {
                URI uri = new URI(pathOrURL);
                pathOrURL = uri.getPath();
            } catch (URISyntaxException e) {
                log.error("parse url failed", e);
            }
        }
        if (pathOrURL.startsWith("/")) {
            pathOrURL = pathOrURL.substring(1);
        }
        String bucket = getBucket();
        //for minos,the bucket prepend before url path
        if (pathOrURL.startsWith(bucket)) {
            pathOrURL = pathOrURL.substring(bucket.length());
        }
        return pathOrURL;
    }

    private static class AsyncUploadTask {
        String ossRelativePath;
        File theUploadFile;
        UploadCallback uploadCallback;

        AsyncUploadTask(String ossRelativePath, File theUploadFile, UploadCallback uploadCallback) {
            this.ossRelativePath = ossRelativePath;
            this.theUploadFile = theUploadFile;
            this.uploadCallback = uploadCallback;
        }
    }

    private static final BlockingQueue<AsyncUploadTask> queue = new LinkedBlockingDeque<>();

    //如果你觉得是大文件，才有必要走异步，如果只是一个jar文件，没必要走异步
    private final Thread asyncTaskThread = new Thread(() -> {
        while (!Thread.currentThread().isInterrupted()) {
            AsyncUploadTask uploadTask;
            try {
                uploadTask = queue.take();
            } catch (InterruptedException e) {
                return;
            }
            Throwable uploadError = null;
            try {
                uploadFile(uploadTask.ossRelativePath, uploadTask.theUploadFile);
            } catch (Throwable throwable) {
                uploadError = throwable;
            }
            try {
                if (uploadError == null) {
                    uploadTask.uploadCallback.onUploadFinished(true, null);
                    log.info("upload oss file:{} success", uploadTask.theUploadFile.getAbsolutePath());
                } else {
                    log.error("error to upload oss file:{}", uploadTask.theUploadFile.getAbsolutePath(), uploadError);
                    uploadTask.uploadCallback.onUploadFinished(false, uploadError);
                }
            } catch (Exception e) {
                log.error("error to call upload callback", e);
            }
        }

    });
}
