package com.virjar.thanos.api.interfaces;

import com.alibaba.fastjson.JSONObject;
import com.virjar.thanos.api.bean.GrabParam;

import java.util.List;

public interface GrabResultParser {
    /**
     * 数据解析器<br>
     *
     * @param grabParam 执行本次抓取使用的抓取参数，解析过程可能需要关联请求和结果
     * @param content   抓取完成后的原始结果
     * @return 解析之后的结果，可能有多条
     */
    List<JSONObject> parse(GrabParam grabParam, String content);
}
