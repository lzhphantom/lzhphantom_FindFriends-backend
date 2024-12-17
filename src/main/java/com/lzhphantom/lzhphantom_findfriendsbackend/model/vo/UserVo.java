package com.lzhphantom.lzhphantom_findfriendsbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户视图对象
 */
@Data
public class UserVo implements Serializable {
    private static final long serialVersionUID = 8471080197617303444L;
    private Long id;

    /**
     * 昵称
     */
    private String username;

    /**
     * 登录账号
     */
    private String loginAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮件
     */
    private String email;

    /**
     * 状态
     */
    private Integer status;

    private String tags;
}
