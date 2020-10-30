package com.virjar.thanos.api.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Md5Utils {

    public static String md5(File file) {
        try {
            return md5(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String md5(String input) {
        return md5(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    public static String md5(InputStream inputStream) {
        byte[] buffer = new byte[1024];
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            int numRead;
            while ((numRead = inputStream.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            return toHexString(md5.digest());
        } catch (Exception e) {
            throw new RuntimeException("md5 error", e);
        }
    }

    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte b1 : b) {
            sb.append(hexChar[((b1 & 0xF0) >>> 4)]);
            sb.append(hexChar[(b1 & 0xF)]);
        }
        return sb.toString();
    }

    private static final char[] hexChar = {'0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'};
}
