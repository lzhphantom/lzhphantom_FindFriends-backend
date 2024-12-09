package com.lzhphantom.lzhphantom_findfriendsbackend.controller;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.BaseResponse;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.ErrorCode;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.ResultUtils;
import com.lzhphantom.lzhphantom_findfriendsbackend.exception.BusinessException;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.User;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.UserLoginRequest;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.UserRegisterRequest;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.lzhphantom.lzhphantom_findfriendsbackend.constants.UserConstant.ADMIN_ROLE;
import static com.lzhphantom.lzhphantom_findfriendsbackend.constants.UserConstant.USER_LOGIN_STATE;


/**
 * 用户接口
 *
 * @author lzhphantom
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public BaseResponse<User> doLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (Objects.isNull(userLoginRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        if (StringUtils.isAnyBlank(userLoginRequest.getUsername(), userLoginRequest.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        return ResultUtils.success(userService.doLogin(userLoginRequest.getUsername(), userLoginRequest.getPassword(), request));
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (Objects.isNull(request)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        return ResultUtils.success(userService.userLogout(request));
    }

    @GetMapping("/currentUser")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (Objects.isNull(userObj)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        User currentUser = (User) userObj;
        User user = userService.getById(currentUser.getId());

        return ResultUtils.success(userService.getSafetyUser(user));
    }

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest dto) {
        if (Objects.isNull(dto)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        if (StringUtils.isAnyBlank(dto.getUserAccount(), dto.getUserPassword(), dto.getCheckPassword(), dto.getUsername())) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        return ResultUtils.success(userService.userRegister(dto));
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUser(String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        LambdaQueryChainWrapper<User> lambdaQuery = userService.lambdaQuery();
        if (StringUtils.isNotEmpty(username)) {
            lambdaQuery.like(User::getUsername, username);
        }
        return ResultUtils.success(lambdaQuery.list()
                .stream().map(userService::getSafetyUser).collect(Collectors.toList()));
    }

    @GetMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteUser(@PathVariable("id") Long id, HttpServletRequest request) {

        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (Objects.isNull(id)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        return ResultUtils.success(userService.removeById(id));
    }

    /**
     * 判断是否为管理员
     *
     * @param request http请求
     * @return 结果
     */
    private boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return Objects.nonNull(user) && user.getRole() == ADMIN_ROLE;
    }
}
