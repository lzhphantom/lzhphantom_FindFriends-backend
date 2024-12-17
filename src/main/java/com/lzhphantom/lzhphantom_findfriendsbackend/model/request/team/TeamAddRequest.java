package com.lzhphantom.lzhphantom_findfriendsbackend.model.request.team;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = 47118829362694146L;
    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态：0-正常，1-私有，2-加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

}
