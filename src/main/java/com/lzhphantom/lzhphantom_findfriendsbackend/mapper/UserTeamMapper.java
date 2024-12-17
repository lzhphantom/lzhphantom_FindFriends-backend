package com.lzhphantom.lzhphantom_findfriendsbackend.mapper;

import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.UserTeam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author lzhphantom
*/
@Mapper
public interface UserTeamMapper extends BaseMapper<UserTeam> {

}




