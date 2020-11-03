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
 * 项目，爬虫数量变多之后进行分类
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="ThanosProject对象", description="项目，爬虫数量变多之后进行分类")
public class ThanosProject implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增主建")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    private String projectDesc;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


    public static final String ID = "id";

    public static final String PROJECT_NAME = "project_name";

    public static final String PROJECT_DESC = "project_desc";

    public static final String CREATE_TIME = "create_time";

}
