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
 * 操作日志，方便用户操作溯源
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="ThanosOpLog对象", description="操作日志，方便用户操作溯源")
public class ThanosOpLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增主建")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "操作账号")
    private String opAccount;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "日志内容")
    private String message;


    public static final String ID = "id";

    public static final String OP_ACCOUNT = "op_account";

    public static final String CREATE_TIME = "create_time";

    public static final String MESSAGE = "message";

}
