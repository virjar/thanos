package com.virjar.thanos.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 爬虫资源管理
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ThanosResource对象", description = "爬虫资源管理")
public class ThanosResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "资源分组，主要区分业务。比如某个网站的账号。account_site")
    private String resourceGroup;

    @ApiModelProperty(value = "资源key，唯一定位资源，比如账号数据对应的手机号: phone")
    private String resourceKey;

    @ApiModelProperty(value = "资源内容,json body存储，比如账号，手机号，名称，token，密码等各种组合")
    private String resourceField;

    @ApiModelProperty(value = "分配次数")
    private Integer allocateCount;

    @ApiModelProperty(value = "资源状态，1:正常 0:禁用 2:锁定")
    private Integer enabled;

    @ApiModelProperty(value = "资源评分，根据历史使用反馈决定这个分数")
    private Integer score;

    @ApiModelProperty(value = "队列调度算法使用，具体参见抓取平台-资源管理-资源调度")
    private Long queueValue;

    @ApiModelProperty(value = "队列调度算法使用，记录本资源上次被分配的时间戳")
    private Long lastUse;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "锁过期时间")
    private LocalDateTime lockExpireTime;


}
