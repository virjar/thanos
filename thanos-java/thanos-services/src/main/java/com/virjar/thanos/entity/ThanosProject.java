package com.virjar.thanos.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 项目，爬虫数量变多之后进行分类
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ThanosProject对象", description = "项目，爬虫数量变多之后进行分类")
public class ThanosProject implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    private String projectDesc;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
