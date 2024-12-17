package com.lzhphantom.lzhphantom_findfriendsbackend.model.request.tag;

import lombok.Data;

import java.io.Serializable;

@Data
public class TagAddRequest implements Serializable {

    private static final long serialVersionUID = -8974270876417875019L;
    /**
     * 标签名
     */
    private String tagName;

    /**
     * 是否为父标签: 0-不是，1-父标签
     */
    private Integer isParent = 0;

    /**
     * 父标签id
     */
    private Long parentId;
}
