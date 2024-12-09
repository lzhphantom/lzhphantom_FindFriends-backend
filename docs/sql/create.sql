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
DROP TABLE IF EXISTS `user`;

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
    isDelete        tinyint  default 0 null comment '是否删除：0-否，1-是',
    role            tinyint  default 0 not null comment '角色'
) comment '用户';
alter table `user`
    add column tags varchar(1024) null comment '标签';