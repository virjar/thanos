package com.virjar.thanos.service.notification;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class GrabNotificationManager {
    private static final Map<String, Class<? extends INotifier>> notifierClasses = new HashMap<>();

    static {
        register(URLPostNotifier.class);
        register(AliyunRocketMqNotifier.class);
    }

    private static void register(Class<? extends INotifier> clazz) {
        ConfigKeys annotation = clazz.getAnnotation(ConfigKeys.class);
        if (annotation == null) {
            log.error("no ConfigKeys annotation described for class: {}", clazz);
            return;
        }
        notifierClasses.put(annotation.type(), clazz);
    }

    public static Map<String, List<String>> supportNotificationConfigures() {
        Map<String, List<String>> ret = Maps.newHashMap();
        for (Class<? extends INotifier> clazz : notifierClasses.values()) {
            ConfigKeys configKeys = clazz.getAnnotation(ConfigKeys.class);
            List<String> configures = Lists.newArrayList(configKeys.value());
            ret.put(configKeys.type(), configures);
        }
        return ret;
    }

    public static boolean hasType(String type) {
        return notifierClasses.containsKey(type);
    }

    public static INotifier createNotifier(String type, Map<String, String> createParam) {
        Class<? extends INotifier> theClass = notifierClasses.get(type);
        if (theClass == null) {
            log.error("no notifier type: {} defined", type);
            return null;
        }
        INotifier notifier;
        try {
            notifier = theClass.newInstance();
        } catch (Exception e) {
            log.error("create notifier instance failed", e);
            throw new RuntimeException(e.getMessage(), e);
        }
        notifier.doConfig(createParam);
        return notifier;
    }

}
