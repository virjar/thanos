package com.virjar.thanos.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 爬虫配置
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ThanosCrawlerConfig对象", description = "爬虫配置")
public class ThanosCrawlerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "爬虫名称，也为爬虫业务id")
    private String configSpace;

    @ApiModelProperty(value = "配置项key")
    private String configKey;

    @ApiModelProperty(value = "配置值")
    private String configValue;

    @ApiModelProperty(value = "对应行号")
    private Integer lineIndex;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
