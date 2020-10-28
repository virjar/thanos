package com.virjar.thanos.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 操作日志，方便用户操作溯源
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ThanosOpLog对象", description = "操作日志，方便用户操作溯源")
public class ThanosOpLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "操作账号")
    private String opAccount;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "日志内容")
    private String message;


}
