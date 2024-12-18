package com.lzhphantom.lzhphantom_findfriendsbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzhphantom.lzhphantom_findfriendsbackend.annotation.Idempotent;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.ErrorCode;
import com.lzhphantom.lzhphantom_findfriendsbackend.exception.BusinessException;
import com.lzhphantom.lzhphantom_findfriendsbackend.mapper.UserMapper;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.User;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.UserRegisterRequest;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.UserTagSearchRequest;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.UserVo;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.UserService;
import com.lzhphantom.lzhphantom_findfriendsbackend.utls.AlgorithmUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

import static com.lzhphantom.lzhphantom_findfriendsbackend.constants.UserConstant.*;


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
    @Idempotent(key = "user:register:")
    public long userRegister(UserRegisterRequest dto) {
        // 校验
        if (StringUtils.isAnyBlank(dto.getUserAccount(), dto.getUserPassword(), dto.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR, "参数为空");
        }
        if ((ReUtil.isMatch("[\\u4e00-\\u9fa5]+", dto.getUsername()) && dto.getUsername().length() < 2)
                || (!ReUtil.isMatch("[\\u4e00-\\u9fa5]+", dto.getUsername()) && dto.getUsername().length() < 4)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称过短");
        }
        if (dto.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名过短");
        }
        if (dto.getUserPassword().length() < 8 || dto.getCheckPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }
        //检验用户名不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        if (dto.getUserAccount().matches(validPattern)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名含有特殊字符");
        }
        //密码是否相同
        if (!dto.getUserPassword().equals(dto.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
        if (lambdaQuery().eq(User::getLoginAccount, dto.getUserAccount()).exists()) {
            throw new BusinessException(ErrorCode.DUPLICATE_ERROR, "账号已存在");
        }
        //密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + dto.getUserPassword()).getBytes());

        //插入数据
        User user = new User();
        user.setLoginAccount(dto.getUserAccount());
        user.setPassword(encryptPassword);
        user.setUsername(dto.getUsername());
        user.setGender(dto.getGender());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setProfile(dto.getProfile());
        if (!save(user)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }


    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }
        //检验用户名不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        if (userAccount.matches(validPattern)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名含有特殊字符");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        User one = lambdaQuery().eq(User::getLoginAccount, userAccount)
                .eq(User::getPassword, encryptPassword).oneOpt()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误"));

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
        handledUser.setTags(one.getTags());
        handledUser.setProfile(one.getProfile());
        return handledUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户 内存运算
     *
     * @param request 标签列
     * @return 用户列
     */
    @Override
    public Page<User> searchUsersByTags(UserTagSearchRequest request) {
        if (CollectionUtils.isEmpty(request.getTagList())) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }

        Page<User> page = lambdaQuery().isNotNull(User::getTags)
                .and(wq -> {
                    request.getTagList().forEach(tag -> wq.like(User::getTags, tag));
                }).page(Page.of(request.getPageNum(), request.getPageSize()));
        page.setRecords(page.getRecords().stream().map(this::getSafetyUser).collect(Collectors.toList()));
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Idempotent(key = "user:update:")
    public Integer updateUser(User user, HttpServletRequest request) {

        User loginUser = getLoginUser(request);
        if (!isAdmin(loginUser) && !loginUser.getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);

        }
        User oldUser = this.baseMapper.selectById(user.getId());
        if (Objects.isNull(oldUser)) {
            throw new BusinessException(ErrorCode.RECORD_NOT_EXIST_ERROR);
        }
        return this.baseMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (Objects.isNull(request)) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (Objects.isNull(userObj)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) userObj;
    }

    /**
     * 判断是否为管理员
     *
     * @param request http请求
     * @return 结果
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return Objects.nonNull(user) && user.getRole() == ADMIN_ROLE;
    }

    /**
     * 判断是否为管理员
     *
     * @param user 用户
     * @return 结果
     */
    @Override
    public boolean isAdmin(User user) {
        return Objects.nonNull(user) && user.getRole() == ADMIN_ROLE;
    }

    @Override
    public List<UserVo> matchUser(long num, User loginUser) {
        List<String> tagList = JSONUtil.toList(loginUser.getTags(), String.class);
        List<User> userList = list().stream().filter(user -> !user.getId().equals(loginUser.getId())).collect(Collectors.toList());
        SortedMap<Long, Double> similarUserMap = new TreeMap<>();
        for (User user : userList) {
            if (user.getTags().isEmpty()) {
                continue;
            }
            List<String> userTagList = JSONUtil.toList(user.getTags(), String.class);
            double similar = AlgorithmUtils.computeJaccardSimilarity(CollUtil.newHashSet(tagList), CollUtil.newHashSet(userTagList));
            similarUserMap.put(user.getId(), similar);
        }
        Map<Long, Double> sortedMap = similarUserMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((o1, o2) -> Double.compare(o2, o1))).limit(num).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<Long> numedIdList = sortedMap.keySet().stream().collect(Collectors.toList());


        return userList.stream().filter(item -> numedIdList.contains(item.getId()))
                .map(item -> BeanUtil.copyProperties(item, UserVo.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Idempotent(key = "user:update:tags:")
    public boolean updateTags(List<String> tags, User loginUser) {
        User user = lambdaQuery().eq(User::getId, loginUser.getId()).one();
        if (Objects.isNull(user)){
            throw new BusinessException(ErrorCode.RECORD_NOT_EXIST_ERROR,"用户不存在");
        }
        if (CollUtil.isNotEmpty(tags) && tags.size()>10){
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR,"标签数量不能超过10");
        }
        user.setTags(JSONUtil.toJsonStr(tags));
        return updateById(user);
    }


    /**
     * 通过sql查询
     *
     * @param tags 标签列
     * @return 用户列表
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        LambdaQueryChainWrapper<User> lambdaQuery = lambdaQuery();
        tags.forEach(item -> {
            lambdaQuery.like(User::getTags, item);
        });
        return lambdaQuery.list().stream().map(this::getSafetyUser).collect(Collectors.toList());
    }
}




