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
 * 爬虫实例jar包，可以被升级和降级
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="ThanosCrawlerJar对象", description="爬虫实例jar包，可以被升级和降级")
public class ThanosCrawlerJar implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增主建")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "爬虫名称，也为爬虫业务id")
    private String crawlerName;

    @ApiModelProperty(value = "是否启用")
    private Boolean enable;

    @ApiModelProperty(value = "该爬虫的版本号")
    private Integer crawlerVersion;

    @ApiModelProperty(value = "爬虫实现体在oss的存储地址")
    private String ossUrl;

    @ApiModelProperty(value = "爬虫jar文件md5,暂时可以考虑作为业务唯一键")
    private String fileMd5;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


    public static final String ID = "id";

    public static final String CRAWLER_NAME = "crawler_name";

    public static final String ENABLE = "enable";

    public static final String CRAWLER_VERSION = "crawler_version";

    public static final String OSS_URL = "oss_url";

    public static final String FILE_MD5 = "file_md5";

    public static final String CREATE_TIME = "create_time";

}
