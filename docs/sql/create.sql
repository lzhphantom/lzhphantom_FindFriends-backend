drop table if exists `tag`;
create table `tag`
(
    `id`          bigint primary key auto_increment comment '主键',
    `tag_name`    varchar(256) unique null comment '标签名',
    `user_id`     bigint              null comment '用户id',
    `parent_id`   bigint              null comment '父标签id',
    `is_parent`   tinyint             null comment '是否为父标签: 0-不是，1-父标签',
    `create_time` datetime  default current_timestamp comment '创建时间',
    `update_time` timestamp default current_timestamp on update current_timestamp,
    `is_delete`   tinyint   default 0 not null comment '是否删除: 0-未删除，1-已删除'
) comment '标签表';
drop table if exists user;
create table user
(
    id              bigint auto_increment primary key comment '主键',
    username        varchar(100)       not null comment '昵称',
    `login_account` varchar(100)       not null comment '登录账号',
    `avatar_url`    varchar(300)       null comment '头像',
    gender          tinyint            null comment '性别',
    password        varchar(100)       null comment '密码',
    phone           varchar(20)        null comment '电话',
    email           varchar(200)       null comment '邮件',
    status          tinyint  default 0 not null comment '状态:0正常',
    `create_time`   datetime default current_timestamp comment '创建时间',
    `update_time`   datetime default current_timestamp on update current_timestamp comment '更新时间',
    `is_delete`     tinyint  default 0 null comment '是否删除：0-否，1-是',
    role            tinyint  default 0 not null comment '角色'
) comment '用户';
# region 交友app
alter table `user`
    add column tags varchar(1024) null comment '标签';
alter table `user`
    add column `profile` varchar(512) null comment '个人简介';

drop table if exists `team`;
create table team
(
    id            bigint auto_increment primary key comment '主键',
    name          varchar(100)       not null comment '队伍名称',
    description   varchar(1024)      null comment '描述',
    max_num        int      default 1 not null comment '最大人数',
    expire_time   datetime           null comment '过期时间',
    user_id       bigint             not null comment '创建人id',
    status        tinyint  default 0 not null comment '状态：0-正常，1-私有，2-加密',
    password      varchar(100)       null comment '密码',
    `create_time` datetime default current_timestamp comment '创建时间',
    `update_time` datetime default current_timestamp on update current_timestamp comment '更新时间',
    `is_delete`   tinyint  default 0 null comment '是否删除：0-否，1-是'
) comment '队伍';

drop table if exists `user_team`;
create table user_team
(
    id            bigint auto_increment primary key comment '主键',
    user_id       bigint             not null comment '用户id',
    team_id       bigint             not null comment '队伍id',
    join_time     datetime           null comment '加入时间',
    `create_time` datetime default current_timestamp comment '创建时间',
    `update_time` datetime default current_timestamp on update current_timestamp comment '更新时间',
    `is_delete`   tinyint  default 0 null comment '是否删除：0-否，1-是'
)comment '用户-队伍';
alter table `team` add column `owner_user_id` bigint not null comment '队长id';

# endregion