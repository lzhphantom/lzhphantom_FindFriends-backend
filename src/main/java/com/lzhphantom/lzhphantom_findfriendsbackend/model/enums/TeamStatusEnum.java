package com.lzhphantom.lzhphantom_findfriendsbackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 队伍状态枚举
 */
@Getter
@AllArgsConstructor
public enum TeamStatusEnum {
    /**
     * 公开
     */
    PUBLIC(0, "公开"),
    /**
     * 私有
     */
    PRIVATE(1, "私有"),
    /**
     * 加密
     */
    ENCRYPTED(2, "加密");

    private final int value;
    private final String text;

    /**
     * 根据值获取枚举
     * @param value 值
     * @return 枚举
     */
    public static TeamStatusEnum getEnumByValue(int value){
        for (TeamStatusEnum teamStatusEnum : TeamStatusEnum.values()) {
            if (teamStatusEnum.value == value){
                return teamStatusEnum;
            }
        }
        return null;
    }
}
