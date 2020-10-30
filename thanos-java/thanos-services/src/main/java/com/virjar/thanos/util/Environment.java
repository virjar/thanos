package com.virjar.thanos.util;

import com.virjar.thanos.api.util.Md5Utils;
import com.virjar.thanos.service.RootConfig;
import com.virjar.thanos.service.engine.OssJarFileManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Environment {
    private static File workingFileDir;

    /**
     * thanos运行文件根目录
     */
    private static final String thanosWorking = "thanos_grab";
    /**
     * oss文件的缓存目录
     */
    private static final String ossFilesDir = "oss_files";

    /**
     * 电脑唯一ID，可由thanos系统自动生成
     */
    private static final String computerIdFile = "computerId.txt";

    /**
     * wrapper的jar包运行目录
     */
    private static final String wrapperJarDir = "wrapper_jar";

    public static File ossCacheDir() {
        return makeSureDirCreated(new File(workingFileDir, ossFilesDir));
    }

    private static File wrapperJarRootDir() {
        return makeSureDirCreated(new File(workingFileDir, wrapperJarDir));
    }

    public static File wrapperJar(String crawlerId, String md5) {
        return new File(wrapperJarDir(crawlerId), md5 + OssJarFileManager.SUFFIX_JAR);
    }

    public static File wrapperJarDir(String crawlerId) {
        return makeSureDirCreated(new File(wrapperJarRootDir(), Md5Utils.md5(crawlerId)));
    }

    private static void makeSureWorkingDir() throws IOException {
        workingFileDir = new File(RootConfig.workingRoot, thanosWorking);
        FileUtils.forceMkdir(workingFileDir);
    }

    private static File makeSureDirCreated(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("can not create dir: " + dir.getAbsolutePath());
            }
        }
        return dir;
    }

    static {
        try {
            makeSureWorkingDir();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static String computerIdInMemory = null;
    private static final String[] sInvalidMac = {"02:00:00:00:00:00", "01:80:C2:00:00:03", "12:34:56:78:9A:BC", "FF:FF:FF:FF:FF:FF", "00:00:00:00:00:00", "00:02:00:00:00:00"};

    /**
     * 为服务器产生一个唯一id，避免分布式冲突问题，如果是众包类型，那么一样存在节点id
     *
     * @return 机器id，生产规则: 缓存 -> mac -> uuid
     */
    public static String computerId() {
        if (StringUtils.isNotBlank(computerIdInMemory)) {
            return computerIdInMemory;
        }

        try {
            File file = new File(workingFileDir, computerIdFile);
            if (file.exists() && file.canRead()) {
                computerIdInMemory = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            }
            if (StringUtils.isNotBlank(computerIdInMemory)) {
                return computerIdInMemory;
            }

            String validMac = null;
            //by mac
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] hardwareAddress = networkInterface.getHardwareAddress();
                if (hardwareAddress == null) {
                    continue;
                }
                StringBuilder macBuffer = new StringBuilder();
                for (int i = 0; i < hardwareAddress.length; i++) {
                    int intValue = hardwareAddress[i];
                    if (intValue < 0) {
                        intValue = 256 + intValue;
                    }
                    macBuffer.append(Integer.toHexString(intValue));
                    if (i < hardwareAddress.length - 1) {
                        macBuffer.append(":");
                    }
                }

                String mac = macBuffer.toString();
                if (mac.length() < 5) {
                    //illegal ? maybe empty
                    continue;
                }

                boolean inValid = false;
                for (String inValidMac : sInvalidMac) {
                    if (inValidMac.equalsIgnoreCase(mac)) {
                        inValid = true;
                        break;
                    }
                }

                if (inValid) {
                    continue;
                }
                validMac = mac;
                break;
            }

            if (StringUtils.isNotBlank(validMac)) {
                computerIdInMemory = validMac + "-" + computerLoginAccount() + "-" + ThreadLocalRandom.current().nextInt(10000);
            } else {
                computerIdInMemory = computerLoginAccount() + "-" + UUID.randomUUID().toString();
            }
            FileUtils.writeStringToFile(file, computerIdInMemory, StandardCharsets.UTF_8);
            return computerIdInMemory;
        } catch (IOException e) {
            //we think this exception will not happen
            //we just throw it if occur finally
            throw new RuntimeException(e);
        }
    }

    private static String computerLoginAccount() {
        return FileUtils.getUserDirectory().getName().trim().replaceAll(" ", "_");
    }
}
