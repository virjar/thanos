use thanos_grab;

create table thanos_crawler
(
    id               bigint(11) primary key auto_increment comment '自增主建',
    crawler_name     varchar(64) not null comment '爬虫名称，也为爬虫业务id',
    project_name     varchar(64)  null comment '所在项目,可以为空',
    enable_version   int         not null default 0 comment '启用的版本',
    owner            varchar (64) null  comment '所属用户',
    description      varchar (128) null  comment '爬虫描述',
    enable           bool not null default true comment '是否启用',
    create_time      datetime     null     default CURRENT_TIMESTAMP comment '创建时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '爬虫的基础信息';

create table thanos_crawler_jar
(
    id              bigint(11) primary key auto_increment comment '自增主建',
    crawler_name    varchar(64) not null comment '爬虫名称，也为爬虫业务id',
    enable          bool                 default 0 comment '是否启用',
    crawler_version int         not null default 0 comment '该爬虫的版本号',
    oss_url         varchar(255)         default '' comment '爬虫实现体在oss的存储地址',
    file_md5        varchar(64)          default '' comment '爬虫jar文件md5,暂时可以考虑作为业务唯一键',
    create_time     datetime    null     default CURRENT_TIMESTAMP comment '创建时间',
    index idx_md5 (file_md5)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '爬虫实例jar包，可以被升级和降级';

create table thanos_project
(
    id               bigint(11) primary key auto_increment comment '自增主建',
    project_name     varchar(64) not null comment '项目名称',
    project_desc varchar(255)  not null default '项目描述',
    create_time      datetime     null     default CURRENT_TIMESTAMP comment '创建时间',
    unique key_project_name (project_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '项目，爬虫数量变多之后进行分类';

create table thanos_crawler_config
(
    id               bigint(11) primary key auto_increment comment '自增主建',
    config_space     varchar(64)  not null comment '爬虫名称，也为爬虫业务id',
    config_key       varchar(64)      not null              comment '配置项key',
    config_value     varchar (512) not null  default '' comment '配置值',
    line_index       int(3) not null default 0 comment '对应行号',
    create_time      datetime     null     default CURRENT_TIMESTAMP comment '创建时间',
    unique key_config_space_key (config_space,config_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '爬虫配置';


CREATE TABLE `thanos_grab_task`
(
    `id`                 bigint(11)  NOT NULL AUTO_INCREMENT COMMENT '自增主建',
    `crawler_name`       varchar(64) NOT NULL COMMENT '爬虫id,引用自grab_crawler',
    `task_id`            varchar(64) NOT NULL COMMENT '任务ID，为业务上面唯一描述一个最小粒度的id，如果过长请转化为md5',
    `task_md5`           varchar(64) NOT NULL COMMENT 'md5(crawler_name+task_id)',
    `task_param`         varchar(255)         DEFAULT '' COMMENT '任务参数，一个抓取任务除开任务id部分数据，如绑定设备、账号等',
    `used_resource`      varchar(255) default '' comment '当次抓取使用的资源',
    `task_status`        tinyint(4)           DEFAULT '0' COMMENT '任务状态，未执行、执行中、执行失败、执行成功',
    `execute_start_time` datetime             DEFAULT NULL COMMENT '上次执行开始时间',
    `execute_end_time`   datetime             DEFAULT NULL COMMENT '上次执行结束时间',
    `last_success_time`  datetime             DEFAULT NULL COMMENT '上次成功执行时间，如果上次执行失败，那么不会刷新抓取结果字段',
    `failed_count`       int(5)     NOT NULL DEFAULT '0' COMMENT '连续失败次数',
    `success_rate`       int(5)              DEFAULT '100' COMMENT '最近连续成功率',
    `executor`           varchar(64)          DEFAULT NULL COMMENT '执行者，分布式环境下区分执行节点',
    `create_time`        datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `failed_message`     varchar(128)         DEFAULT NULL COMMENT '失败原因',
    `invalid_count`      int(5)      NOT NULL DEFAULT 0 COMMENT '种子非法次数',
    PRIMARY KEY (`id`),
    UNIQUE KEY `key_task_id` (`crawler_name`, `task_id`),
    KEY `idx_id` (`task_id`),
    KEY `idx_start_time` (`execute_start_time`),
    KEY `idx_execute_end_time` (`execute_end_time`) USING BTREE,
    KEY `idx_id_time` (`crawler_name`, `execute_start_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='爬虫任务，和seed相同概念';


  create table thanos_resource
(
    id               bigint(11) primary key auto_increment comment '自增主建',
    resource_group   varchar(64)   not null comment '资源分组，主要区分业务。比如某个网站的账号。account_site',
    resource_key     varchar(64)   not null comment '资源key，唯一定位资源，比如账号数据对应的手机号: phone',
    resource_field   varchar(1024) not null comment '资源内容,json body存储，比如账号，手机号，名称，token，密码等各种组合',
    allocate_count   int           not null default 0 comment '分配次数',
    enabled          tinyint(3)    not null default 1 comment '资源状态，1:正常 0:禁用 2:锁定',
    score            int           not null default 100 comment '资源评分，根据历史使用反馈决定这个分数',
    queue_value      bigint(11)    not null default 0 comment '队列调度算法使用，具体参见抓取平台-资源管理-资源调度',
    last_use         bigint(11)    not null default 0 comment '队列调度算法使用，记录本资源上次被分配的时间戳',
    create_time      datetime      null     default CURRENT_TIMESTAMP comment '创建时间',
    lock_expire_time datetime      null comment '锁过期时间',
    unique key_resource_id (resource_group, resource_key),
    index idx_queue (queue_value)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '爬虫资源管理';


create table thanos_kv_cache
(
    id          bigint(11) primary key auto_increment comment '自增主建',
    cache_group varchar(64) not null default 'grab_system' comment '业务分组，避免多个业务冲突，默认使用公共分组',
    cache_key   varchar(64) not null comment 'key',
    cache_value text comment 'cache内容',
    create_time datetime    null     default CURRENT_TIMESTAMP comment '创建时间',
    expire      datetime    not null comment '数据过期时间',
    unique uniq_cache_id (cache_group, cache_key),
    index idx_expire (expire)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '爬虫系统提供的一个简单的kv缓存';

  create table thanos_user
(
    id          bigint(11) primary key auto_increment comment '自增主建',
    account     varchar(50) not null comment '账号',
    pwd         varchar(50) not null comment '密码',
    login_token varchar(255) default null comment '用户登陆的token，api访问的凭证',
    last_active datetime     default null comment '用户最后活跃时间',
    is_admin    bool         default false comment '是否是管理员',
    nick_name   varchar(50) comment '用户昵称',
    blocked     bool         default false comment '是否被冻结'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '系统用户，需要有账号才能进入本系统';

create table thanos_op_log
(
    id          bigint(11) primary key auto_increment comment '自增主建',
    op_account  varchar(50)  not null comment '操作账号',
    create_time datetime     null     default CURRENT_TIMESTAMP comment '创建时间',
    message     varchar(255) not null default '' comment '日志内容'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '操作日志，方便用户操作溯源';

create table thanos_server
(
    id                 bigint(11) primary key auto_increment comment '自增主建',
    executor           varchar(64)  null comment '执行者，分布式环境下区分执行节点',
    enable             bool                  default true comment '是否生效',
    master             bool default  false  comment '是否是master节点',
    server_comment     varchar(255) not null default '备注',
    last_survival_time datetime     null comment '最后的服务器存活时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '抓取服务器节点，主要用作分片';

create table thanos_notifier_config
(
    id          bigint(11) primary key auto_increment comment '自增主建',
    name        varchar(64)   not null comment '通知器名称，业务id。不可重复',
    type        varchar(32)   not null comment '通知类型，为枚举值。com.virjar.thanos.service.notification.ConfigKeys.type',
    config      varchar(1024) not null default '{}' comment '配置内容，map。其key为：com.virjar.thanos.service.notification.ConfigKeys.value',
    scope       varchar(512)  not null default '*' comment '配置作用域，那些爬虫被当前配置作用，默认为全局',
    enable      bool                   default true comment '是否生效',
    update_time datetime      null comment '更新时间，内存根据这个时间觉得是否reload通知器模型',
    unique uniq_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '抓取完通知配置';

CREATE TABLE `thanos_task_blob`
(
    `id`             bigint(11)  NOT NULL AUTO_INCREMENT COMMENT '自增主建',
    `crawler_name`   varchar(64) NOT NULL COMMENT '爬虫id,引用自grab_crawler',
    `task_id`        varchar(64) NOT NULL COMMENT '任务ID，为业务上面唯一描述一个最小粒度的id，如果过长请转化为md5',
    `task_md5`       varchar(64) NOT NULL COMMENT '业务id，是grab_task表中，crawler_name,task_id两个字段连接的md5',
    `execute_result` text COMMENT '执行结果',
    `grab_log`       text COMMENT '抓取日志',
    PRIMARY KEY (`id`),
    UNIQUE KEY `key_task_id` (`task_md5`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='爬虫任务的数据区域，由于数据内容会比较大。所以单独抽离出来，避免任务调度的时候影响性能';