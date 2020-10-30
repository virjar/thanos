package com.virjar.thanos.api;


import com.virjar.thanos.api.bean.CrawlerMeta;
import com.virjar.thanos.api.bean.GrabParam;
import com.virjar.thanos.api.bean.GrabResult;

public interface GrabProcessor {

    /**
     * 抓取任务抽象
     *
     * @param grabParam 抓取参数
     * @return 抓取结果
     */
    GrabResult process(GrabParam grabParam);


    /**
     * 在这里描述爬虫的相关配置参数，以便框架区分爬虫以及控制爬虫的生命周期
     *
     * @return 配置内容
     */
    CrawlerMeta config();
}
