package com.virjar.thanos.entity.vo;

import com.virjar.thanos.api.bean.CrawlerMeta;
import com.virjar.thanos.entity.ThanosCrawler;
import com.virjar.thanos.entity.ThanosCrawlerConfig;
import com.virjar.thanos.service.RootConfig;
import com.virjar.thanos.service.engine.GrabProcessorHandler;
import com.virjar.thanos.service.engine.GrabProcessorLoader;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.TreeMap;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "GrabCrawlerModel对象", description = "爬虫实例")
public class GrabCrawlerModel {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增主建")
    private Long id;

    @ApiModelProperty(value = "爬虫名称，也为爬虫业务id")
    private String crawlerName;

    @ApiModelProperty(value = "是否启用")
    private Boolean enable;

    @ApiModelProperty(value = "该爬虫的版本号")
    private Integer crawlerVersion;

    @ApiModelProperty(value = "爬虫实现体在oss的存储地址")
    private String ossUrl;

    @ApiModelProperty(value = "爬虫jar文件md5,暂时可以考虑作为业务唯一键")
    private String fileMd5;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "缓存时间，超出缓存需要重耍任务，-1代表缓存永久有效")
    private Integer cacheDuration;

    @ApiModelProperty(value = "默认重试次数")
    private Integer retryTimes;

    @ApiModelProperty(value = "最大并发数量，QPS单位")
    private Integer qps;

    @ApiModelProperty(value = "任务超时时间，单位秒")
    private Integer timeout;

    @ApiModelProperty(value = "cron表达式，描述爬虫执行时间规则")
    private String cronExp;

    @ApiModelProperty(value = "该爬虫需要的资源，逗号分割。如 account_site,device_android")
    private String resourceDeclare;

    @ApiModelProperty(value = "爬虫配置的id")
    private Long configId;

    @ApiModelProperty(value = "爬虫jar包的id")
    private Long crawlerJarId;

    private String description = "";

    private String documentUrl = "";

    private String demoURI = "";

    private String wrapperDes = "";

    public static GrabCrawlerModel create(ThanosCrawler grabCrawler, ThanosCrawlerConfig thanosCrawlerConfig) {
//        GrabCrawlerModel ret = new GrabCrawlerModel();
//        ret.setCrawlerName(grabCrawler.getCrawlerName());
//        ret.setId(thanosCrawlerConfig.getId());
//        ret.setCrawlerVersion(grabCrawler.getCrawlerVersion());
//        ret.setOssUrl(grabCrawler.get());
//        ret.setFileMd5(grabCrawler.getFileMd5());
//
//        ret.setEnable(thanosCrawlerConfig.getEnable());
//        ret.setCacheDuration(thanosCrawlerConfig.getCacheDuration());
//        ret.setRetryTimes(thanosCrawlerConfig.getRetryTimes());
//        ret.setQps(thanosCrawlerConfig.getQps());
//        ret.setTimeout(thanosCrawlerConfig.getTimeout());
//        ret.setCronExp(thanosCrawlerConfig.getCronExp());
//        ret.setCreateTime(thanosCrawlerConfig.getCreateTime());
//
//        CommonRes<GrabProcessorHandler> commonRes = GrabProcessorLoader.loadProcessorFromModel(ret);
//        if (commonRes.isOk()) {
//            CrawlerMeta config = commonRes.getData().config();
//            ret.setResourceDeclare(commonRes.getData().rebuildResourceDeclare());
//            ret.setDescription(config.getDescription());
//            ret.setDocumentUrl(config.getDocumentUrl());
//
//        }
//
//        ret.setConfigId(thanosCrawlerConfig.getId());
//        ret.setCrawlerJarId(grabCrawler.getId());
//        return ret;
        //TODO
        return null;
    }

}
