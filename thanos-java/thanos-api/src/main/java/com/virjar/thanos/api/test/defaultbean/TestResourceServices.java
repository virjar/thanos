package com.virjar.thanos.api.test.defaultbean;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.virjar.thanos.api.GrabLogger;
import com.virjar.thanos.api.bean.ResourceMeta;
import com.virjar.thanos.api.services.ResourceServices;
import com.virjar.thanos.api.test.TestBean;
import com.virjar.thanos.api.util.SimpleHttpInvoker;

import java.util.Map;

@TestBean
public class TestResourceServices implements ResourceServices {

    //由于有内存锁的存在，所以本地调试环境下，apiEndpoint只能指向单台服务器，不能指向ng地址。否则可能存在一致性问题
    public static String apiEndpoint = "http://172.17.67.134:8080";
    private ResourceServices resourceServices;


    @Override
    public ResourceMeta allocate(String group, int expire) {
        return getResource(apiEndpoint
                + "/api/grab-platform/grab-resource/allocate?" +
                "group=" + group);
    }

    @Override
    public boolean lock(String group, String key, int expireSecond) {
        return true;
    }

    @Override
    public ResourceMeta query(String group, String key) {
        return getResource(apiEndpoint
                + "/api/grab-platform/grab-resource/getResource?" +
                "group=" + group +
                "&key=" + key
        );
    }

    @Override
    public void unlock(String group, String key) {
        SimpleHttpInvoker.get(apiEndpoint
                + "/api/grab-platform/grab-resource/unlock?" +
                "group=" + group +
                "&key=" + key
        );


    }

    @Override
    public void feedback(ResourceMeta resourceMeta, boolean good) {
        //to nothing in test environment
    }

    @Override
    public void updateContent(ResourceMeta resourceMeta) {
        Map<String, String> param = Maps.newHashMap();
        param.put("group", resourceMeta.getGroup());
        param.put("key", resourceMeta.getKey());
        param.put("content", resourceMeta.getField());
        SimpleHttpInvoker.post(apiEndpoint
                + "/api/grab-platform/grab-resource/upDateResource", param
        );
    }

    @Override
    public void forbid(ResourceMeta resourceMeta) {
        //to nothing in test environment
    }

    private ResourceMeta getResource(String url) {
        String response = SimpleHttpInvoker.get(url);
        if (response == null) {
            GrabLogger.error("resource allocate api call failed");
            return null;
        }
        GrabLogger.info("resource get response:" + response);
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (!jsonObject.getBoolean("ok")) {

            return null;
        }
        JSONObject resourceJson = jsonObject.getJSONObject("data");
        ResourceMeta resourceMeta = new ResourceMeta(
                resourceJson.getString("resourceGroup"),
                resourceJson.getString("resourceKey"),
                resourceJson.getString("resourceField")
        );
        resourceMeta.setResourceServices(this);
        return resourceMeta;
    }

}
