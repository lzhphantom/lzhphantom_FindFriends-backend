package com.lzhphantom.lzhphantom_findfriendsbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 父标签表视图
 */
@Data
public class TagTreeVo implements Serializable {
    private Long id;
    private String text;
    private List<TagTreeChildVo> children;

}