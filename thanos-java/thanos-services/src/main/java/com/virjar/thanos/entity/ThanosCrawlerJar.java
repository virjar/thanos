package com.virjar.thanos.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 爬虫实例jar包，可以被升级和降级
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ThanosCrawlerJar对象", description = "爬虫实例jar包，可以被升级和降级")
public class ThanosCrawlerJar implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private LocalDateTime createTime;


}
