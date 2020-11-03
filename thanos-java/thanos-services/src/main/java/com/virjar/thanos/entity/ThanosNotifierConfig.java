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
 * 抓取完通知配置
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="ThanosNotifierConfig对象", description="抓取完通知配置")
public class ThanosNotifierConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增主建")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "通知器名称，业务id。不可重复")
    private String name;

    @ApiModelProperty(value = "通知类型，为枚举值。com.virjar.thanos.service.notification.ConfigKeys.type")
    private String type;

    @ApiModelProperty(value = "配置内容，map。其key为：com.virjar.thanos.service.notification.ConfigKeys.value")
    private String config;

    @ApiModelProperty(value = "配置作用域，那些爬虫被当前配置作用，默认为全局")
    private String scope;

    @ApiModelProperty(value = "是否生效")
    private Boolean enable;

    @ApiModelProperty(value = "更新时间，内存根据这个时间觉得是否reload通知器模型")
    private LocalDateTime updateTime;


    public static final String ID = "id";

    public static final String NAME = "name";

    public static final String TYPE = "type";

    public static final String CONFIG = "config";

    public static final String SCOPE = "scope";

    public static final String ENABLE = "enable";

    public static final String UPDATE_TIME = "update_time";

}
