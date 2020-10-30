package com.virjar.thanos.api.services;


import com.virjar.thanos.api.bean.Seed;

/**
 * 抓取过程中，产生了新的任务，那么通过这个接口发布到系统中
 */
public interface TaskPushService {
    /**
     * @param crawlerId 可指定爬虫ID入库，正常情况选择当前爬虫,注意不要随意污染其他爬虫源
     * @param seed      结构化的种子数据
     */
    void pushNewSeed(String crawlerId, Seed seed);
}
