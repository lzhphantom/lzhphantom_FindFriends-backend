package com.lzhphantom.lzhphantom_findfriendsbackend.model.dto;

import com.lzhphantom.lzhphantom_findfriendsbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamQuery extends PageRequest implements Serializable {

    private static final long serialVersionUID = -2772439770210008527L;
    /**
     * 主键
     */
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
     * 搜索词：包含队伍名称和描述
     */
    private String searchText;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 状态：0-正常，1-私有，2-加密
     */
    private Integer status;

    private Long ownerUserId;

}
