package com.lzhphantom.lzhphantom_findfriendsbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzhphantom.lzhphantom_findfriendsbackend.annotation.Idempotent;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.ErrorCode;
import com.lzhphantom.lzhphantom_findfriendsbackend.constants.TagConstant;
import com.lzhphantom.lzhphantom_findfriendsbackend.exception.BusinessException;
import com.lzhphantom.lzhphantom_findfriendsbackend.mapper.TagMapper;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.Tag;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.User;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.tag.TagAddRequest;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.tag.TagUpdateRequest;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.TagTreeChildVo;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.TagTreeVo;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.TagVo;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.TagService;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lzhphantom
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {
    private final UserService userService;

    @Override
    @Transactional
    @Idempotent(key = "tag:add:")
    public long addTag(TagAddRequest tagAddRequest, User loginUser) {
        if (ObjUtil.isNull(tagAddRequest) && ObjUtil.isNull(tagAddRequest.getTagName())) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        if (ObjUtil.isNull(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (lambdaQuery().eq(Tag::getTagName, tagAddRequest.getTagName()).exists()) {
            throw new BusinessException(ErrorCode.DUPLICATE_ERROR, "改标签已存在");
        }
        if (tagAddRequest.getIsParent() == TagConstant.NOT_TAG_PARENT
                && ObjUtil.isNull(tagAddRequest.getParentId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "父标签必须选择");
        }
        Tag newRecord = BeanUtil.copyProperties(tagAddRequest, Tag.class);
        newRecord.setUserId(loginUser.getId());
        boolean save = save(newRecord);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建标签失败");
        }
        return newRecord.getId();
    }

    @Override
    @Transactional
    @Idempotent(key = "tag:update:")
    public boolean updateTag(TagUpdateRequest tagUpdateRequest, User loginUser) {
        if (ObjUtil.isNull(tagUpdateRequest) || ObjUtil.isNull(tagUpdateRequest.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        if (ObjUtil.isNull(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Tag oldTag = getById(tagUpdateRequest.getId());
        if (oldTag.getIsParent() == TagConstant.TAG_PARENT && tagUpdateRequest.getIsParent() != TagConstant.TAG_PARENT) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "父标签不能修改为非父标签");
        }
        if (!StrUtil.equals(oldTag.getTagName(), tagUpdateRequest.getTagName())
                && lambdaQuery().eq(Tag::getTagName, tagUpdateRequest.getTagName())
                .ne(Tag::getId, tagUpdateRequest.getId()).exists()) {
            throw new BusinessException(ErrorCode.DUPLICATE_ERROR, "该标签已存在");
        }
        Tag newTag = BeanUtil.copyProperties(tagUpdateRequest, Tag.class);
        newTag.setUserId(loginUser.getId());
        return updateById(newTag);
    }

    @Override
    public List<TagTreeVo> listTagTree(String tagName) {
        List<TagTreeVo> result = lambdaQuery().eq(Tag::getIsParent, TagConstant.TAG_PARENT).list()
                .stream().map(tag -> {
                    TagTreeVo tagTreeVo = new TagTreeVo();
                    tagTreeVo.setId(tag.getId());
                    tagTreeVo.setText(tag.getTagName());
                    return tagTreeVo;
                }).collect(Collectors.toList());
        List<Long> parentIdList = result.stream().map(TagTreeVo::getId).collect(Collectors.toList());
        LambdaQueryChainWrapper<Tag> wrapper = lambdaQuery();
        if (StrUtil.isNotEmpty(tagName)){
            wrapper.like(Tag::getTagName, tagName);
        }
        Map<Long, List<TagTreeChildVo>> childGroups = wrapper.in(Tag::getParentId, parentIdList).list()
                .stream().collect(Collectors.groupingBy(Tag::getParentId, Collectors.mapping(item -> {
                    TagTreeChildVo child = new TagTreeChildVo();
                    child.setId(item.getTagName());
                    child.setText(item.getTagName());
                    return child;
                }, Collectors.toList())));
        result.forEach(item -> {
            item.setChildren(childGroups.get(item.getId()));
        });
        if (StrUtil.isEmpty(tagName)){
            return result;
        }

        return result.stream().filter(item-> !CollUtil.isEmpty(item.getChildren())).collect(Collectors.toList());
    }

    @Override
    public List<TagVo> listTag(String tagName) {
        if (StrUtil.isEmpty(tagName)){
            return BeanUtil.copyToList(list(),TagVo.class);
        }
        List<TagVo> result = BeanUtil.copyToList(lambdaQuery().like(Tag::getTagName, tagName).list(), TagVo.class);
        Set<Long> parentIdList = result.stream().map(TagVo::getParentId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<TagVo> parentList = BeanUtil.copyToList(lambdaQuery().in(Tag::getId, parentIdList).list(), TagVo.class);
        result.addAll(parentList);
        return result;
    }
}




