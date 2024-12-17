package com.lzhphantom.lzhphantom_findfriendsbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzhphantom.lzhphantom_findfriendsbackend.annotation.Idempotent;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.ErrorCode;
import com.lzhphantom.lzhphantom_findfriendsbackend.exception.BusinessException;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.Team;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.User;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.UserTeam;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.dto.TeamQuery;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.enums.TeamStatusEnum;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.team.*;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.TeamUserVo;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.TeamVo;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.UserVo;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.TeamService;
import com.lzhphantom.lzhphantom_findfriendsbackend.mapper.TeamMapper;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.UserService;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.UserTeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lzhphantom
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {
    private final UserTeamService userTeamService;
    private final UserService userService;

    /**
     * 添加队伍
     *
     * @param team      队伍信息
     * @param loginUser 当前用户
     * @return 队伍id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Idempotent(key = "team:add:")
    public long addTeam(TeamAddRequest team, final User loginUser) {
        if (ObjUtil.isNull(team)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        if (ObjUtil.isNull(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //校验参数
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "人数不符合要求");
        }
        if (StrUtil.isEmpty(team.getName()) || team.getName().length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名字不符合要求");
        }
        if (StrUtil.isNotEmpty(team.getDescription()) && team.getDescription().length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不符合要求");
        }
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (ObjUtil.isNull(statusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不符合要求");
        }

        if (TeamStatusEnum.ENCRYPTED.equals(statusEnum)) {
            String password = team.getPassword();
            if (StrUtil.isEmpty(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不符合要求");
            }
        }
        if (ObjUtil.isNull(team.getExpireTime()) && team.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间不符合要求");
        }
        // 校验用户最多创建数量
        Long creatTeamNum = lambdaQuery().eq(Team::getUserId, loginUser.getId()).count();
        if (creatTeamNum >= 5) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "最多创建或加入5个队伍");
        }
        Team copied = BeanUtil.copyProperties(team, Team.class);
        copied.setUserId(loginUser.getId());
        copied.setOwnerUserId(loginUser.getId());
        boolean save = save(copied);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUser.getId());
        userTeam.setTeamId(copied.getId());
        userTeam.setJoinTime(LocalDateTime.now());
        save = userTeamService.save(userTeam);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        return copied.getId();
    }

    /**
     * 搜索队伍
     *
     * @param teamQuery 查询条件
     * @param isAdmin   是否是管理员
     * @return
     */
    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {

        if (ObjUtil.isNull(teamQuery)) {
            return this.baseMapper.selectAllTeamsWithUsersOptimized().stream().filter(item -> {
                if (!isAdmin) {
                    return TeamStatusEnum.PRIVATE.equals(TeamStatusEnum.getEnumByValue(item.getStatus()));
                }
                return true;
            }).collect(Collectors.toList());
        }
        LambdaQueryChainWrapper<Team> wrapper = lambdaQuery();
        wrapper.and(qw -> qw.gt(Team::getExpireTime, LocalDateTime.now()).or().isNull(Team::getExpireTime));
        //校验参数
        Long id = teamQuery.getId();
        if (ObjUtil.isNotNull(id)) {
            if (id <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            wrapper.eq(Team::getId, id);
        }

        String name = teamQuery.getName();
        if (StrUtil.isNotEmpty(name)) {
            wrapper.like(Team::getName, name);
        }
        String description = teamQuery.getDescription();
        if (StrUtil.isNotEmpty(description)) {
            wrapper.like(Team::getDescription, description);
        }
        String searchText = teamQuery.getSearchText();
        if (StrUtil.isNotEmpty(searchText)) {
            wrapper.and(qw -> qw.like(Team::getName, searchText).or().like(Team::getDescription, searchText));
        }
        Integer maxNum = teamQuery.getMaxNum();
        if (ObjUtil.isNotNull(maxNum)) {
            if (maxNum < 0 || maxNum > 20) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            wrapper.eq(Team::getMaxNum, maxNum);
        }
        Long userId = teamQuery.getUserId();
        if (ObjUtil.isNotNull(userId)) {
            if (userId <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            wrapper.eq(Team::getUserId, userId);
        }
        Integer status = teamQuery.getStatus();
        if (ObjUtil.isNotNull(status)) {
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (ObjUtil.isNull(statusEnum)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }

            wrapper.eq(Team::getStatus, status);
        }
        if (!isAdmin) {
            wrapper.ne(Team::getStatus, TeamStatusEnum.PRIVATE.getValue());
        }
        //查询队伍
//        return this.baseMapper.selectTeamsWithDynamicQuery(teamQuery);
        List<Team> list = wrapper.list();
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }

        List<Long> teamIdList = list.stream().map(Team::getId).collect(Collectors.toList());
        List<UserTeam> userTeams = userTeamService.lambdaQuery().in(UserTeam::getTeamId, teamIdList).list();
        Map<Long, List<Long>> teamContainUsers = userTeams.stream().collect(Collectors.groupingBy(UserTeam::getTeamId, Collectors.mapping(UserTeam::getUserId, Collectors.toList())));

        Set<Long> allUser = userTeams.stream().map(UserTeam::getUserId).collect(Collectors.toSet());
        Map<Long, UserVo> userVoMap = userService.listByIds(allUser).stream()
                .map(item -> BeanUtil.copyProperties(item, UserVo.class))
                .collect(Collectors.toMap(UserVo::getId, item -> item));

        return list.stream().map(item -> {
            TeamUserVo vo = BeanUtil.copyProperties(item, TeamUserVo.class);
            List<UserVo> userVoList = teamContainUsers.get(vo.getId()).stream().map(userVoMap::get).collect(Collectors.toList());
            vo.setUserList(userVoList);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 更新队伍信息
     *
     * @param request   修改队伍信息
     * @param loginUser 登录用户
     * @return 修改成功
     */
    @Override
    @Transactional
    @Idempotent(key = "team:update:")
    public boolean updateTeam(TeamUpdateRequest request, User loginUser) {

        if (ObjUtil.isNull(request)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        Long id = request.getId();
        if (ObjUtil.isNull(id) || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldRecord = this.baseMapper.selectById(id);
        if (ObjUtil.isNull(oldRecord)) {
            throw new BusinessException(ErrorCode.RECORD_NOT_EXIST);
        }
        if (!oldRecord.getOwnerUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Integer status = request.getStatus();
        if (ObjUtil.isNotNull(status)) {
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (ObjUtil.isNull(statusEnum)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            if (TeamStatusEnum.PUBLIC.equals(statusEnum)) {
                request.setPassword("");
            }
            if (TeamStatusEnum.ENCRYPTED.equals(statusEnum) && StrUtil.isEmpty(request.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密模式下，必须设置密码");
            }
        }

        //todo 判断新值和老值是否一致,一致不用更新，减少数据库压力
        Team copied = BeanUtil.copyProperties(request, Team.class);

        return updateById(copied);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest 加入队伍信息
     * @param loginUser       登录用户
     * @return 加入成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Idempotent(key = "team:join:")
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (ObjUtil.isNull(teamJoinRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        if (ObjUtil.isNull(teamJoinRequest.getId()) || teamJoinRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.baseMapper.selectById(teamJoinRequest.getId());
        if (ObjUtil.isNull(team)) {
            throw new BusinessException(ErrorCode.RECORD_NOT_EXIST, "队伍不存在");
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(team.getStatus());
        if (Objects.equals(statusEnum, TeamStatusEnum.PRIVATE)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "禁止加入私有队伍");
        }
        if (Objects.equals(statusEnum, TeamStatusEnum.ENCRYPTED) && !StrUtil.equals(team.getPassword(), teamJoinRequest.getPassword())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "密码不正确");
        }
        boolean exists = userTeamService.lambdaQuery().eq(UserTeam::getUserId, loginUser.getId())
                .eq(UserTeam::getTeamId, team.getId()).exists();
        if (exists) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已加入该队伍");
        }
        Long currentUserJoinTeamNum = userTeamService.lambdaQuery().eq(UserTeam::getUserId, loginUser.getId()).count();
        if (currentUserJoinTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入 5 个队伍");
        }
        Long currentTeamUserNum = userTeamService.lambdaQuery().eq(UserTeam::getTeamId, team.getId()).count();
        if (currentTeamUserNum >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUser.getId());
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(LocalDateTime.now());
        return userTeamService.save(userTeam);
    }

    /**
     * 退出队伍
     *
     * @param teamQuitRequest 退出队伍信息
     * @param loginUser       登录用户
     * @return 退出成功
     */
    @Override
    @Transactional
    @Idempotent(key = "team:quit:")
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (ObjUtil.isNull(teamQuitRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        Long teamId = teamQuitRequest.getId();
        if (ObjUtil.isNull(teamId) || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.baseMapper.selectById(teamId);
        if (ObjUtil.isNull(team)) {
            throw new BusinessException(ErrorCode.RECORD_NOT_EXIST);
        }
        boolean exists = userTeamService.lambdaQuery().eq(UserTeam::getUserId, loginUser.getId())
                .eq(UserTeam::getTeamId, teamId).exists();
        if (!exists) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入该队伍");
        }
        Long currentTeamUserNum = userTeamService.lambdaQuery().eq(UserTeam::getTeamId, teamId).count();
        if (currentTeamUserNum == 1) {
            //最后一人，删除队伍
            boolean success = removeById(teamId);
            if (!success) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
            }
            return userTeamService.lambdaUpdate().eq(UserTeam::getTeamId, teamId).remove();
        } else {
            if (team.getOwnerUserId().equals(loginUser.getId())) {
                //是队长，转移关系
                List<UserTeam> userTeams = userTeamService.lambdaQuery().eq(UserTeam::getTeamId, teamId)
                        .orderByAsc(UserTeam::getJoinTime).last("limit 2").list();
                if (CollUtil.isEmpty(userTeams) || userTeams.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍人数少于两人");
                }
                UserTeam nextOwner = userTeams.get(1);
                team.setOwnerUserId(nextOwner.getUserId());
                boolean success = updateById(team);
                if (!success) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍信息失败");
                }
            }
            return userTeamService.lambdaUpdate().eq(UserTeam::getUserId, loginUser.getId())
                    .eq(UserTeam::getTeamId, teamId).remove();
        }
    }

    /**
     * 解散队伍
     *
     * @param teamDeleteRequest 解散队伍信息
     * @param loginUser         登录用户
     * @return 解散成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Idempotent(key = "team:delete:")
    public boolean deleteTeam(TeamDeleteRequest teamDeleteRequest, User loginUser) {
        if (ObjUtil.isNull(teamDeleteRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        Long teamId = teamDeleteRequest.getId();
        if (ObjUtil.isNull(teamId) || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.baseMapper.selectById(teamId);
        if (ObjUtil.isNull(team)) {
            throw new BusinessException(ErrorCode.RECORD_NOT_EXIST);
        }
        if (!team.getOwnerUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean removeTeamUsers = userTeamService.lambdaUpdate().eq(UserTeam::getTeamId, teamId).remove();
        if (!removeTeamUsers) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        }
        return removeById(teamId);
    }

    /**
     * 获取我创建的队伍
     *
     * @param loginUser 登录用户
     * @return 队伍列表
     */
    @Override
    public List<TeamUserVo> myTeams(User loginUser) {
        if (ObjUtil.isNull(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<Team> teams = lambdaQuery().eq(Team::getOwnerUserId, loginUser.getId()).list();

        List<Long> teamIds = teams.stream().map(Team::getId).collect(Collectors.toList());
        return getTeamUserVos(teamIds, teams);

    }

    /**
     * 获取我加入的队伍
     *
     * @param loginUser 登录用户
     * @return 队伍列表
     */
    @Override
    public List<TeamUserVo> myJoinTeams(User loginUser) {
        if (ObjUtil.isNull(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<Long> teamIds = userTeamService.lambdaQuery().eq(UserTeam::getUserId, loginUser.getId()).list()
                .stream().map(UserTeam::getTeamId).collect(Collectors.toList());
        List<Team> teams = lambdaQuery().in(Team::getId, teamIds).list();
        return getTeamUserVos(teamIds, teams);
    }

    private List<TeamUserVo> getTeamUserVos(List<Long> teamIds, List<Team> teams) {
        if (CollUtil.isEmpty(teamIds)) {
            return Collections.emptyList();
        }
        List<UserTeam> userTeams = userTeamService.lambdaQuery().in(UserTeam::getTeamId, teamIds).list();
        Map<Long, List<Long>> teamContainUsers = userTeams.stream().collect(Collectors.groupingBy(UserTeam::getTeamId, Collectors.mapping(UserTeam::getUserId, Collectors.toList())));
        Set<Long> allUser = userTeams.stream().map(UserTeam::getUserId).collect(Collectors.toSet());
        Map<Long, UserVo> userVoMap = userService.listByIds(allUser).stream()
                .map(item -> BeanUtil.copyProperties(item, UserVo.class))
                .collect(Collectors.toMap(UserVo::getId, item -> item));


        return teams.stream().map(item -> {
            TeamUserVo vo = BeanUtil.copyProperties(item, TeamUserVo.class);
            List<UserVo> userVoList = teamContainUsers.get(vo.getId()).stream().map(userVoMap::get).collect(Collectors.toList());
            vo.setUserList(userVoList);
            return vo;
        }).collect(Collectors.toList());
    }
}




