package com.virjar.thanos.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 抓取服务器节点，主要用作分片
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ThanosServer对象", description = "抓取服务器节点，主要用作分片")
public class ThanosServer implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "执行者，分布式环境下区分执行节点")
    private String executor;

    @ApiModelProperty(value = "是否生效")
    private Boolean enable;

    @ApiModelProperty(value = "是否是master节点")
    private Boolean master;

    private String serverComment;

    @ApiModelProperty(value = "最后的服务器存活时间")
    private LocalDateTime lastSurvivalTime;


}
