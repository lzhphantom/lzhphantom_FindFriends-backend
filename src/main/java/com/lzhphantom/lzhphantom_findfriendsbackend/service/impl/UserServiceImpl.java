package com.lzhphantom.lzhphantom_findfriendsbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.ErrorCode;
import com.lzhphantom.lzhphantom_findfriendsbackend.exception.BusinessException;
import com.lzhphantom.lzhphantom_findfriendsbackend.mapper.UserMapper;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.User;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.UserRegisterRequest;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static com.lzhphantom.lzhphantom_findfriendsbackend.constants.UserConstant.SALT;
import static com.lzhphantom.lzhphantom_findfriendsbackend.constants.UserConstant.USER_LOGIN_STATE;


/**
 * 用户表逻辑
 *
 * @author lzhphantom
 */
@Service
@Log4j2
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    @Transactional
    public long userRegister(UserRegisterRequest dto) {
        // 校验
        if (StringUtils.isAnyBlank(dto.getUserAccount(), dto.getUserPassword(), dto.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR,"参数为空");
        }
        if (dto.getUsername().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"昵称过短");
        }
        if (dto.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名过短");
        }
        if (dto.getUserPassword().length() < 8 || dto.getCheckPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        //检验用户名不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        if (dto.getUserAccount().matches(validPattern)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名含有特殊字符");
        }
        //密码是否相同
        if (!dto.getUserPassword().equals(dto.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }
        if (lambdaQuery().eq(User::getLoginAccount, dto.getUserAccount()).exists()) {
            throw new BusinessException(ErrorCode.DUPLICATE_ERROR,"账号已存在");
        }
        //密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + dto.getUserPassword()).getBytes());

        //插入数据
        User user = new User();
        user.setLoginAccount(dto.getUserAccount());
        user.setPassword(encryptPassword);
        user.setUsername(dto.getUsername());
        if (!save(user)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败");
        }
        return user.getId();
    }

    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR,"参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        //检验用户名不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        if (userAccount.matches(validPattern)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名含有特殊字符");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        User one = lambdaQuery().eq(User::getLoginAccount, userAccount)
                .eq(User::getPassword, encryptPassword).oneOpt()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误"));

        //用户脱敏
        User safetyUser = getSafetyUser(one);


        //记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param one 源用户
     * @return 脱敏用户
     */
    @Override
    public User getSafetyUser(User one) {
        if (Objects.isNull(one)) {
            return null;
        }
        User handledUser = new User();
        handledUser.setId(one.getId());
        handledUser.setUsername(one.getUsername());
        handledUser.setLoginAccount(one.getLoginAccount());
        handledUser.setAvatarUrl(one.getAvatarUrl());
        handledUser.setGender(one.getGender());
        handledUser.setPhone(one.getPhone());
        handledUser.setEmail(one.getEmail());
        handledUser.setCreateTime(one.getCreateTime());
        handledUser.setRole(one.getRole());
        return handledUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




