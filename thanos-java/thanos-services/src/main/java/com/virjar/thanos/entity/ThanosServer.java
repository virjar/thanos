package com.virjar.thanos.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 抓取服务器节点，主要用作分片
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ThanosServer对象", description = "抓取服务器节点，主要用作分片")
public class ThanosServer implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增主建")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "执行者，分布式环境下区分执行节点")
    private String executor;

    @ApiModelProperty(value = "是否生效")
    private Boolean enable;

    @ApiModelProperty(value = "是否是master节点")
    private Boolean master;

    private String serverComment;

    @ApiModelProperty(value = "最后的服务器存活时间")
    private LocalDateTime lastSurvivalTime;


    public static final String ID = "id";

    public static final String EXECUTOR = "executor";

    public static final String ENABLE = "enable";

    public static final String MASTER = "master";

    public static final String SERVER_COMMENT = "server_comment";

    public static final String LAST_SURVIVAL_TIME = "last_survival_time";


    public boolean isAlive() {
        LocalDateTime lastSurvivalTime = getLastSurvivalTime();
        if (lastSurvivalTime == null) {
            //可能是null，测试环境遇到过
            return false;
        }
        return lastSurvivalTime.isAfter(LocalDateTime.now().minusSeconds(30));
    }
}
