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
 * 爬虫的基础信息
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="ThanosCrawler对象", description="爬虫的基础信息")
public class ThanosCrawler implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增主建")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "爬虫名称，也为爬虫业务id")
    private String crawlerName;

    @ApiModelProperty(value = "所在项目,可以为空")
    private String projectName;

    @ApiModelProperty(value = "启用的版本")
    private Integer enableVersion;

    @ApiModelProperty(value = "所属用户")
    private String owner;

    @ApiModelProperty(value = "爬虫描述")
    private String description;

    @ApiModelProperty(value = "是否启用")
    private Boolean enable;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


    public static final String ID = "id";

    public static final String CRAWLER_NAME = "crawler_name";

    public static final String PROJECT_NAME = "project_name";

    public static final String ENABLE_VERSION = "enable_version";

    public static final String OWNER = "owner";

    public static final String DESCRIPTION = "description";

    public static final String ENABLE = "enable";

    public static final String CREATE_TIME = "create_time";

}
