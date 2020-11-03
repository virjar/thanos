package com.virjar.thanos.service.notification;

import com.alibaba.fastjson.JSONObject;
import com.virjar.thanos.api.bean.ResourceMeta;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 抓取任务结束之后，发送mq消息出去，对抓取系统感兴趣的系统可以通过监听这个消息获取实时的抓取数据
 */
@Data
public class GrabFinishMessage {

    /**
     * 对应爬虫id
     */
    private String crawlerId;
    /**
     * 抓取种子ID
     */
    private String seedId;

    /**
     * 种子md5，在抓取系统全局唯一，可以用来进行数据查询
     */
    private String taskMd5;

    /**
     * 抓取参数
     */
    private Map<String, Object> grabParam;
    /**
     * 本次抓取用到的资源
     */
    private Map<String, ResourceMeta> bindResource;

    /**
     * 本次抓取是否成功，即使失败也会通知。如果你只对抓取成功的数据感兴趣，那么通过这个flag过滤掉失败的即可
     */
    private boolean isSuccess;

    /**
     * 抓取回来的原始报文，数据解析之前,如果业务觉得解析后的数据不满足自己的需要，那么可以自行解析这个数据
     */
    private String rawResponse;


    /**
     * 数据解析结果，这是抓取端的解析器实现数据初步解析
     */
    private List<JSONObject> parsedResponse;


    /**
     * 抓取完成时间
     */
    private long executeEndTime;

    /**
     * 本条数据是那个爬虫版本抓取的，一般情况使用方不需要关心。除非抓取方在抓取或者解析上面有不兼容发布或者回滚，那么适用方可能需要根据版本做特殊适配。
     * <br>
     * 请注意，如果是众包抓取，那么可能没有爬虫版本的概念，此时crawlerVersion为固定值：-2
     */
    private int crawlerVersion;

}
