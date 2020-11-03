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
 * 爬虫配置
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="ThanosCrawlerConfig对象", description="爬虫配置")
public class ThanosCrawlerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增主建")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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


    public static final String ID = "id";

    public static final String CONFIG_SPACE = "config_space";

    public static final String CONFIG_KEY = "config_key";

    public static final String CONFIG_VALUE = "config_value";

    public static final String LINE_INDEX = "line_index";

    public static final String CREATE_TIME = "create_time";

}
