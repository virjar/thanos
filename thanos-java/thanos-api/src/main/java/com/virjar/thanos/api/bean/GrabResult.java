package com.virjar.thanos.api.bean;


import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GrabResult {
    private Object content;
    private int errorCode;
    private String errorMessage;
    private List<JSONObject> parsedContent;

    public static final int ERROR_CODE_INVALID_SEED = -2;

    public static GrabResult invalid = new GrabResult(null, ERROR_CODE_INVALID_SEED, "invalidSeed seed", null);

    public static GrabResult invalidSeed() {
        return GrabResult.invalid;
    }

    public static GrabResult invalidSeed(String errorMessage) {
        return new GrabResult(null, ERROR_CODE_INVALID_SEED, errorMessage, null);
    }

    public static GrabResult failed(String msg) {
        return new GrabResult(null, -1, msg, null);
    }

    public static GrabResult success(Object content) {
        return new GrabResult(content, 0, null, null);
    }
}
