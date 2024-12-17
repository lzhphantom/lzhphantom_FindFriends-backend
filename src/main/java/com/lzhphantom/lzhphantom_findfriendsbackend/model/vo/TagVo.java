package com.lzhphantom.lzhphantom_findfriendsbackend.model.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签表
 */
@Data
public class TagVo implements Serializable {
    private static final long serialVersionUID = 293068770917055869L;
    /**
     * 主键
     */
    private Long id;

    /**
     * 标签名
     */
    private String tagName;

    /**
     * 父标签id
     */
    private Long parentId;

    /**
     * 是否为父标签: 0-不是，1-父标签
     */
    private Integer isParent;


}