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
 * 系统用户，需要有账号才能进入本系统
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="ThanosUser对象", description="系统用户，需要有账号才能进入本系统")
public class ThanosUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增主建")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "账号")
    private String account;

    @ApiModelProperty(value = "密码")
    private String pwd;

    @ApiModelProperty(value = "用户登陆的token，api访问的凭证")
    private String loginToken;

    @ApiModelProperty(value = "用户最后活跃时间")
    private LocalDateTime lastActive;

    @ApiModelProperty(value = "是否是管理员")
    private Boolean isAdmin;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "是否被冻结")
    private Boolean blocked;


    public static final String ID = "id";

    public static final String ACCOUNT = "account";

    public static final String PWD = "pwd";

    public static final String LOGIN_TOKEN = "login_token";

    public static final String LAST_ACTIVE = "last_active";

    public static final String IS_ADMIN = "is_admin";

    public static final String NICK_NAME = "nick_name";

    public static final String BLOCKED = "blocked";

}
