package com.virjar.thanos.api.interfaces;


import com.alibaba.fastjson.JSONObject;
import com.virjar.thanos.api.bean.Seed;

public interface SeedKeyResolver {
    /**
     * 当我们迁移任务数据的时候，需要为任务内容计算一个任务id
     *
     * @param seed 种子数据
     * @return 该种子的唯一键
     */
    Seed resolveSeedKey(JSONObject seed);
}
