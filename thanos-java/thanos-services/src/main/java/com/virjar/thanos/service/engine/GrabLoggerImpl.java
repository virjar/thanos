package com.virjar.thanos.service.engine;

import com.virjar.thanos.api.GrabLogger;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GrabLoggerImpl implements GrabLogger.ILogger {
    private final StringBuilder buildLogger = new StringBuilder();
    private static final String LEVEL_INFO = "INFO:";
    private static final String LEVEL_WARING = "WARN:";
    private static final String LEVEL_ERROR = "ERROR:";

    public String logContent() {
        return buildLogger.toString();
    }

    @Override
    public void info(String info) {
        log.info(info);
        outLog(LEVEL_INFO, info, null);
    }

    @Override
    public void waring(String info) {
        log.info(info);
        outLog(LEVEL_WARING, info, null);
    }

    @Override
    public void error(String info) {
        log.info(info);
        outLog(LEVEL_ERROR, info, null);
    }

    @Override
    public void info(String info, Throwable throwable) {
        log.info(info, throwable);
        outLog(LEVEL_INFO, info, throwable);
    }

    @Override
    public void waring(String info, Throwable throwable) {
        log.info(info, throwable);
        outLog(LEVEL_WARING, info, throwable);
    }

    @Override
    public void error(String info, Throwable throwable) {
        //请注意，抓取的异常日志，我们认为是业务异常，比如抓取的网络超时
        //这类异常在抓取系统中正常且普遍存在，所以不应该把这类异常输出到系统的异常日志文件中
        //我们认为抓取的error异常日志为该爬虫普通日志
        log.info(info, throwable);
        outLog(LEVEL_ERROR, info, throwable);
    }

    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";

    private static final ThreadLocal<Map<String, SimpleDateFormat>> simpleDataFormatThreadLocal = new InheritableThreadLocal<>();

    private static SimpleDateFormat getOrCreate() {
        Map<String, SimpleDateFormat> map = simpleDataFormatThreadLocal.get();
        if (map == null) {
            map = new HashMap<>();
            simpleDataFormatThreadLocal.set(map);
        }
        SimpleDateFormat simpleDateFormat = map.get(dateFormat);
        if (simpleDateFormat != null) {
            return simpleDateFormat;
        }
        simpleDateFormat = new SimpleDateFormat(dateFormat);
        map.put(dateFormat, simpleDateFormat);
        return simpleDateFormat;
    }

    private void outLog(String type, String message, Throwable throwable) {
        buildLogger.append(type)
                .append(getOrCreate().format(new Date()))
                .append(":")
                .append(message).append("\n");
        if (throwable != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream);
            throwable.printStackTrace(new PrintWriter(outputStreamWriter));
            buildLogger.append(byteArrayOutputStream.toString());
            buildLogger.append("\n");
        }
    }
}
