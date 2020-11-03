package com.virjar.thanos.service.engine;

import com.virjar.thanos.api.GrabProcessor;
import com.virjar.thanos.api.util.ClassScanner;
import com.virjar.thanos.api.util.CommonUtil;
import com.virjar.thanos.entity.vo.CommonRes;
import com.virjar.thanos.entity.vo.GrabCrawlerModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

@Slf4j
public class GrabProcessorLoader {
    private static final Map<String, ProcessorHolder> cache = new ConcurrentHashMap<>();

    private static class ProcessorHolder {
        private final String md5;
        private final GrabProcessorHandler grabProcessorHandler;

        ProcessorHolder(String md5, GrabProcessorHandler grabProcessorHandler) {
            this.md5 = md5;
            this.grabProcessorHandler = grabProcessorHandler;
        }
    }

    public static CommonRes<GrabProcessorHandler> loadProcessorFromModel(GrabCrawlerModel grabCrawler) {
        ProcessorHolder processorHolder = cache.get(grabCrawler.getCrawlerName());
        if (processorHolder != null && processorHolder.md5.equalsIgnoreCase(grabCrawler.getFileMd5())) {
            //hinted cache
            return CommonRes.success(processorHolder.grabProcessorHandler);
        }

        try {
            File downloadJar = OssJarFileManager.getInstance().downloadJar(grabCrawler.getFileMd5(), grabCrawler.getCrawlerName(), grabCrawler.getOssUrl());
            CommonRes<GrabProcessorHandler> commonRes = loadProcessorFromFile(downloadJar);
            if (!commonRes.isOk()) {
                return commonRes;
            }

            //cache it
            cache.put(grabCrawler.getCrawlerName(), new ProcessorHolder(grabCrawler.getFileMd5(), commonRes.getData()));
            return commonRes;
        } catch (IOException e) {
            log.error("loadProcessorFromModel error occur ", e);
            return CommonRes.failed(e);
        }
    }


    public static CommonRes<GrabProcessorHandler> loadProcessorFromFile(File targetFile) throws IOException {
        GrabProcessor grabProcessor;
        //check illegal
        try (JarFile jarFile = new JarFile(targetFile)) {
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{targetFile.toURI().toURL()}, GrabProcessor.class.getClassLoader());
            ClassLoader originClassLoader = Thread.currentThread().getContextClassLoader();
            ClassScanner.SubClassVisitor<GrabProcessor> classVisitor = new ClassScanner.SubClassVisitor<>(true, GrabProcessor.class);
            try {
                Thread.currentThread().setContextClassLoader(urlClassLoader);
                ClassScanner.scanJarFile(jarFile, classVisitor);
            } finally {
                Thread.currentThread().setContextClassLoader(originClassLoader);
            }

            List<Class<? extends GrabProcessor>> crawlerBeanClasses = classVisitor.getSubClass();
            if (crawlerBeanClasses.isEmpty()) {
                return CommonRes.failed("no GrabProcessor found in this jar file");
            }
            if (crawlerBeanClasses.size() != 1) {
                return CommonRes.failed("multi GrabProcessor[" + StringUtils.join(crawlerBeanClasses, ",") + "] found in this jar file");
            }

            try {
                grabProcessor = crawlerBeanClasses.get(0).newInstance();
            } catch (Exception e) {
                return CommonRes.failed("can not create instance for class :" + crawlerBeanClasses.get(0).getName() + ", " + CommonUtil.getStackTrack(e));
            }
            return CommonRes.success(new GrabProcessorHandler(grabProcessor, crawlerBeanClasses.get(0)));
        }
    }
}
