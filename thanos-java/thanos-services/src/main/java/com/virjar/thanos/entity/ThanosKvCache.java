package com.virjar.thanos.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 爬虫系统提供的一个简单的kv缓存
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ThanosKvCache对象", description = "爬虫系统提供的一个简单的kv缓存")
public class ThanosKvCache implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务分组，避免多个业务冲突，默认使用公共分组")
    private String cacheGroup;

    @ApiModelProperty(value = "key")
    private String cacheKey;

    @ApiModelProperty(value = "cache内容")
    private String cacheValue;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "数据过期时间")
    private LocalDateTime expire;


}
