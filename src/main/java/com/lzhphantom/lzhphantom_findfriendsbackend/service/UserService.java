package com.lzhphantom.lzhphantom_findfriendsbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.User;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.UserRegisterRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lzhphantom
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2024-12-06 17:14:55
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param dto 用户注册类
     * @return 用户ID
     */
    long userRegister(UserRegisterRequest dto);

    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request      请求
     * @return 用户信息
     */
    User doLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param one 原用户
     * @return 脱敏用户
     */
    User getSafetyUser(User one);

    /**
     * 用户注销
     * @param request 请求
     */
    int userLogout(HttpServletRequest request);
}
