package com.virjar.thanos.api.bean;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrabParam {
    private String seedId;
    private JSONObject param = new JSONObject();
    private Map<String, ResourceMeta> bindResource = new HashMap<>();

    public String getParam(String key) {
        return param.getString(key);
    }

    public void setParam(String key, Object value) {
        param.put(key, value);
    }

    public ResourceMeta getResource(String key) {
        if (bindResource == null) {
            return null;
        }
        return bindResource.get(key);
    }

}
