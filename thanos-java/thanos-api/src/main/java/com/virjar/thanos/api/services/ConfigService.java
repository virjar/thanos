package com.virjar.thanos.api.services;

public interface ConfigService {
    /**
     * 配置中心，提供热发配置的能力，目前只支持单项配置传递，不支持监听器
     *
     * @param key          配置key
     * @param defaultValue 默认值
     * @return value
     */
    String get(String key, String defaultValue);
}
