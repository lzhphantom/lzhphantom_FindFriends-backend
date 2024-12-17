package com.lzhphantom.lzhphantom_findfriendsbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 子标签表视图
 */
@Data
public class TagTreeChildVo implements Serializable {
    private static final long serialVersionUID = 2044894063226437898L;
    private String id;
    private String text;

}