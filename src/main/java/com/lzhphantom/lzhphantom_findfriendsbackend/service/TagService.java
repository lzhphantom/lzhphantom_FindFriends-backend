package com.lzhphantom.lzhphantom_findfriendsbackend.service;

import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.Tag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.User;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.tag.TagAddRequest;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.tag.TagUpdateRequest;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.TagTreeVo;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.TagVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lzhphantom
*/
public interface TagService extends IService<Tag> {

    /**
     * 添加标签
     * @param tagAddRequest 标签信息
     * @param loginUser 当前用户
     * @return 新增标签id
     */
    long addTag(TagAddRequest tagAddRequest, User loginUser);

    /**
     * 更新标签
     * @param tagUpdateRequest 标签信息
     * @param loginUser 当前用户
     * @return 结果
     */
    boolean updateTag(TagUpdateRequest tagUpdateRequest, User loginUser);

    /**
     * 获取标签树
     * @param tagName 标签名称
     * @return 结果树
     */
    List<TagTreeVo> listTagTree(String tagName);

    /**
     * 获取标签列表
     * @param tagName 标签名称
     * @return 结果列表
     */
    List<TagVo> listTag(String tagName);
}
