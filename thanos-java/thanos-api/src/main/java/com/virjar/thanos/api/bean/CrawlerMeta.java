package com.virjar.thanos.api.bean;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrawlerMeta {
    private String crawlerId;

    private int crawlerVersion = 0;
    @Builder.Default
    private int cacheDuration = -1;
    @Builder.Default
    private int retryTimes = 3;
    @Builder.Default
    private int qps = 5;
    @Builder.Default
    private int timeout = 120;
    @Builder.Default
    private String description = "请在wrapper中添加描述";
    @Builder.Default
    private String documentUrl = "接口文档未添加";
    @Builder.Default
    private String resourceDeclare = "";
}
