package com.virjar.thanos.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 爬虫任务，和seed相同概念
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ThanosGrabTask对象", description = "爬虫任务，和seed相同概念")
public class ThanosGrabTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增主建")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "爬虫id,引用自grab_crawler")
    private String crawlerName;

    @ApiModelProperty(value = "任务ID，为业务上面唯一描述一个最小粒度的id，如果过长请转化为md5")
    private String taskId;

    @ApiModelProperty(value = "md5(crawler_name+task_id)")
    private String taskMd5;

    @ApiModelProperty(value = "任务参数，一个抓取任务除开任务id部分数据，如绑定设备、账号等")
    private String taskParam;

    @ApiModelProperty(value = "当次抓取使用的资源")
    private String usedResource;

    @ApiModelProperty(value = "任务状态，未执行、执行中、执行失败、执行成功")
    private Integer taskStatus;

    @ApiModelProperty(value = "上次执行开始时间")
    private LocalDateTime executeStartTime;

    @ApiModelProperty(value = "上次执行结束时间")
    private LocalDateTime executeEndTime;

    @ApiModelProperty(value = "上次成功执行时间，如果上次执行失败，那么不会刷新抓取结果字段")
    private LocalDateTime lastSuccessTime;

    @ApiModelProperty(value = "连续失败次数")
    private Integer failedCount;

    @ApiModelProperty(value = "最近连续成功率")
    private Integer successRate;

    @ApiModelProperty(value = "执行者，分布式环境下区分执行节点")
    private String executor;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "失败原因")
    private String failedMessage;

    @ApiModelProperty(value = "种子非法次数")
    private Integer invalidCount;


    public static final String ID = "id";

    public static final String CRAWLER_NAME = "crawler_name";

    public static final String TASK_ID = "task_id";

    public static final String TASK_MD5 = "task_md5";

    public static final String TASK_PARAM = "task_param";

    public static final String USED_RESOURCE = "used_resource";

    public static final String TASK_STATUS = "task_status";

    public static final String EXECUTE_START_TIME = "execute_start_time";

    public static final String EXECUTE_END_TIME = "execute_end_time";

    public static final String LAST_SUCCESS_TIME = "last_success_time";

    public static final String FAILED_COUNT = "failed_count";

    public static final String SUCCESS_RATE = "success_rate";

    public static final String EXECUTOR = "executor";

    public static final String CREATE_TIME = "create_time";

    public static final String FAILED_MESSAGE = "failed_message";

    public static final String INVALID_COUNT = "invalid_count";

}
