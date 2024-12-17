package com.lzhphantom.lzhphantom_findfriendsbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = -7299439595994088526L;
    /**
     * 页面大小
     */
    protected int pageSize = 10;
    /**
     * 当前是第几页
     */
    protected int pageNum = 1;
}
