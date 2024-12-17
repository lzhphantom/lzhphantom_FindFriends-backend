package com.lzhphantom.lzhphantom_findfriendsbackend.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 队伍和用户信息封装类
 *
 * @author lzhphantom
 */
@Data
public class TeamUserVo implements Serializable {
    private static final long serialVersionUID = -2440804043549655877L;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
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
     * 创建人id
     */
    private Long userId;

    /**
     * 状态：0-正常，1-私有，2-加密
     */
    private Integer status;

    /**
     * 队伍用户列表
     */
    private List<UserVo> userList;

    /**
     * 创建人
     */
    private Long ownerUserId;

}
