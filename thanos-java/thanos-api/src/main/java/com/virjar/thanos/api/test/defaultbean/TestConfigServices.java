package com.virjar.thanos.api.test.defaultbean;


import com.virjar.thanos.api.services.ConfigService;
import com.virjar.thanos.api.test.TestBean;

@TestBean
public class TestConfigServices implements ConfigService {
    @Override
    public String get(String key, String defaultValue) {
        return null;
    }
}
