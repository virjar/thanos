package com.virjar.thanos.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 爬虫任务的数据区域，由于数据内容会比较大。所以单独抽离出来，避免任务调度的时候影响性能
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ThanosTaskBlob对象", description = "爬虫任务的数据区域，由于数据内容会比较大。所以单独抽离出来，避免任务调度的时候影响性能")
public class ThanosTaskBlob implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "爬虫id,引用自grab_crawler")
    private String crawlerName;

    @ApiModelProperty(value = "任务ID，为业务上面唯一描述一个最小粒度的id，如果过长请转化为md5")
    private String taskId;

    @ApiModelProperty(value = "业务id，是grab_task表中，crawler_name,task_id两个字段连接的md5")
    private String taskMd5;

    @ApiModelProperty(value = "执行结果")
    private String executeResult;

    @ApiModelProperty(value = "抓取日志")
    private String grabLog;


}
