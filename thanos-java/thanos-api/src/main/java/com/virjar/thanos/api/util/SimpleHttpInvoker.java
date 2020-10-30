package com.virjar.thanos.api.util;

import com.virjar.thanos.api.GrabLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SimpleHttpInvoker {
    public static String get(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return parseResponse(connection);
        } catch (Exception e) {
            GrabLogger.error("error for url:" + url, e);
            return null;
        }
    }

    private static String parseResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            connection.disconnect();
            return null;
        }
        try (InputStream inputStream = connection.getInputStream()) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } finally {
            connection.disconnect();
        }
    }

    public static String post(String url, Map<String, String> param) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            try (OutputStream outputStream = connection.getOutputStream()) {

                String body = URLEncodedUtils.format(param, StandardCharsets.UTF_8);
                IOUtils.write(body, outputStream, StandardCharsets.UTF_8);
                return parseResponse(connection);
            }
        } catch (Exception e) {
            GrabLogger.error("error for url:" + url, e);
            return null;
        }
    }
}
