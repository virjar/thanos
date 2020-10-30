package com.virjar.thanos.api.bean;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.virjar.thanos.api.services.ResourceServices;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ResourceMeta {
    private final String group;
    private final String key;
    private String field;

    @Setter
    private ResourceServices resourceServices;

    public ResourceMeta(String group, String key, String field) {
        this.group = group;
        this.key = key;
        this.field = field;
    }

    private JSONObject contentJson = null;

    public JSONObject getResourceContent() {
        if (contentJson != null) {
            return contentJson;
        }
        contentJson = JSON.parseObject(field);
        return contentJson;
    }

    public String getParam(String key) {
        return getResourceContent().getString(key);
    }

    public boolean lock(int expireSecond) {
        return resourceServices.lock(group, key, expireSecond);
    }

    public void unlock() {
        resourceServices.unlock(group, key);
    }

    public void updateContent() {
        field = contentJson.toJSONString();
        resourceServices.updateContent(this);
    }

    public void updateContent(Object newContent) {
        if (newContent == null) {
            return;
        }
        JSONObject newJson = (JSONObject) JSONObject.toJSON(newContent);
        contentJson.putAll(newJson);
        updateContent();
    }

    public <T> T toContent(Class<T> clazz) {
        return getResourceContent().toJavaObject(clazz);
    }
}