package com.lzhphantom.lzhphantom_findfriendsbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.BaseResponse;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.ErrorCode;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.ResultUtils;
import com.lzhphantom.lzhphantom_findfriendsbackend.exception.BusinessException;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.Team;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.User;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.dto.TeamQuery;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.request.team.*;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.TeamUserVo;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.TeamVo;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.TeamService;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/team")
@Log4j2
public class TeamController {
    private final TeamService teamService;
    private final UserService userService;

    /**
     * 创建队伍
     *
     * @param teamAddRequest 队伍参数
     * @return 队伍id
     */
    @PostMapping("/addTeam")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (Objects.isNull(teamAddRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);

        long id = teamService.addTeam(teamAddRequest, loginUser);

        return ResultUtils.success(id);
    }

    /**
     * 解散队伍
     * @param teamDeleteRequest 队伍参数
     * @param request 请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody TeamDeleteRequest teamDeleteRequest, HttpServletRequest request) {
        if (ObjUtil.isNull(teamDeleteRequest)){
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        boolean remove = teamService.deleteTeam(teamDeleteRequest, userService.getLoginUser(request));
        if (!remove) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新队伍信息
     * @param teamUpdateRequest 更新队伍参数
     * @param request 请求
     * @return 更新结果
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (ObjUtil.isNull(teamUpdateRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        boolean update = teamService.updateTeam(teamUpdateRequest, userService.getLoginUser(request));
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<TeamVo> getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (ObjUtil.isNull(team)) {
            throw new BusinessException(ErrorCode.RECORD_NOT_EXIST);
        }
        return ResultUtils.success(BeanUtil.copyProperties(team, TeamVo.class));
    }

    /**
     * 搜索队伍
     * @param teamQuery 队伍查询参数
     * @param request 请求
     * @return 队伍列表
     */
    @PostMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeams(@RequestBody TeamQuery teamQuery, HttpServletRequest request) {
        if (ObjUtil.isNull(teamQuery)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }

        return ResultUtils.success(teamService.listTeams(teamQuery, userService.isAdmin(request)));
    }

    @PostMapping("/list/Page")
    public BaseResponse<Page<Team>> listTeamPage(@RequestBody TeamQuery teamQuery) {
        if (ObjUtil.isNull(teamQuery)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        Team teamQueryCondition = BeanUtil.copyProperties(teamQuery, Team.class);
        QueryWrapper<Team> wrapper = new QueryWrapper<>(teamQueryCondition);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        Page<Team> teamPage = teamService.page(page, wrapper);
        return ResultUtils.success(teamPage);
    }

    /**
     * 加入队伍
     * @param teamUserVo 参数
     * @param request 请求
     * @return 加入结果
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamUserVo, HttpServletRequest request) {
        if (ObjUtil.isNull(teamUserVo)){
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(teamService.joinTeam(teamUserVo, loginUser));
    }

    /**
     * 退出队伍
     * @param teamQuitRequest 参数
     * @param request 请求
     * @return 退出结果
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (ObjUtil.isNull(teamQuitRequest)){
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(teamService.quitTeam(teamQuitRequest, loginUser));
    }

    /**
     * 获取我创建的队伍
     * @param request 请求
     * @return 队伍列表
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVo>> listMyCreateTeam(HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);

        return ResultUtils.success(teamService.myTeams(loginUser));
    }

    /**
     * 获取我加入的队伍
     * @param request 请求
     * @return 队伍列表
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVo>> listMyJoinTeam(HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);

        return ResultUtils.success(teamService.myJoinTeams(loginUser));
    }
    //获取某队伍的已加入用户列表
    //获取某队伍的已加入用户id列表
}
