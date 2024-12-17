package com.lzhphantom.lzhphantom_findfriendsbackend.controller;

import cn.hutool.core.util.ObjUtil;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.BaseResponse;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.ErrorCode;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.ResultUtils;
import com.lzhphantom.lzhphantom_findfriendsbackend.exception.BusinessException;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.Tag;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.User;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.BaseDeleteRequest;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.tag.TagAddRequest;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.tag.TagUpdateRequest;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.TagTreeVo;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.TagVo;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.TagService;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tag")
public class TagController {

    private final TagService tagService;
    private final UserService userService;

    /**
     * 添加标签
     * @param tagAddRequest 标签信息
     * @param request 请求
     * @return 添加结果
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTag(@RequestBody TagAddRequest tagAddRequest, HttpServletRequest request){
        if (ObjUtil.isNull(tagAddRequest)){
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(tagService.addTag(tagAddRequest,loginUser));
    }

    /**
     * 删除标签
     * @param baseDeleteRequest 信息
     * @param request 请求
     * @return 结果
     */
    @PostMapping("/remove")
    public BaseResponse deleteTag(@RequestBody BaseDeleteRequest baseDeleteRequest,HttpServletRequest request){
        boolean isAdmin = userService.isAdmin(request);
        if (!isAdmin){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (!tagService.lambdaQuery().eq(Tag::getId,baseDeleteRequest.getId()).exists()){
            throw new BusinessException(ErrorCode.RECORD_NOT_EXIST);
        }
        return ResultUtils.success(tagService.removeById(baseDeleteRequest.getId()));
    }
    //修改标签
    @PostMapping("/update")
    public BaseResponse updateTag(@RequestBody TagUpdateRequest tagUpdateRequest, HttpServletRequest request){
        if (ObjUtil.isNull(tagUpdateRequest)){
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(tagService.updateTag(tagUpdateRequest,loginUser));
    }

    /**
     * 标签列表
     * @param tagName 标签名称
     * @return 结果
     */
    @GetMapping("/list")
    public BaseResponse<List<TagVo>> listTag(String tagName){
        return ResultUtils.success(tagService.listTag(tagName));
    }

    /**
     * 获取标签树
     * @param tagName 标签名称
     * @return 结果
     */
    @GetMapping("/tree")
    public BaseResponse<List<TagTreeVo>> listTagTree(String tagName){
        return ResultUtils.success(tagService.listTagTree(tagName));
    }
}
