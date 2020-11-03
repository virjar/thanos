package com.virjar.thanos.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 爬虫系统提供的一个简单的kv缓存
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="ThanosKvCache对象", description="爬虫系统提供的一个简单的kv缓存")
public class ThanosKvCache implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增主建")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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


    public static final String ID = "id";

    public static final String CACHE_GROUP = "cache_group";

    public static final String CACHE_KEY = "cache_key";

    public static final String CACHE_VALUE = "cache_value";

    public static final String CREATE_TIME = "create_time";

    public static final String EXPIRE = "expire";

}
