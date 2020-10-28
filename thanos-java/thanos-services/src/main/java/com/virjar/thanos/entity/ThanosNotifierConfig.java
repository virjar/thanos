package com.virjar.thanos.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 抓取完通知配置
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ThanosNotifierConfig对象", description = "抓取完通知配置")
public class ThanosNotifierConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "通知器名称，业务id。不可重复")
    private String name;

    @ApiModelProperty(value = "通知类型，为枚举值。com.czb.crawl.grab.service.notification.ConfigKeys.type")
    private String type;

    @ApiModelProperty(value = "配置内容，map。其key为：com.czb.crawl.grab.service.notification.ConfigKeys.value")
    private String config;

    @ApiModelProperty(value = "配置作用域，那些爬虫被当前配置作用，默认为全局")
    private String scope;

    @ApiModelProperty(value = "是否生效")
    private Boolean enable;

    @ApiModelProperty(value = "更新时间，内存根据这个时间觉得是否reload通知器模型")
    private LocalDateTime updateTime;


}
