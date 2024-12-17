package com.lzhphantom.lzhphantom_findfriendsbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.Team;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.dto.TeamQuery;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.TeamUserVo;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.vo.UserVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author lzhphantom
 */
@Mapper
public interface TeamMapper extends BaseMapper<Team> {
    @Results(
            id = "userTeamMapper",
            value = {
                    @Result(column = "id", property = "id", id = true),
                    @Result(column = "name", property = "name"),
                    @Result(column = "description", property = "description"),
                    @Result(column = "max_num", property = "maxNum"),
                    @Result(column = "expire_time", property = "expireTime"),
                    @Result(column = "user_id", property = "userId"),
                    @Result(column = "status", property = "status"),
                    @Result(property = "userList", javaType = List.class, many = @Many(select = "selectTeamUserList"), column = "id")
            }
    )
    @Select("select id, name, description, max_num, expire_time, user_id, status, owner_user_id\n" +
            "from team t " +
            "where t.is_delete=0 and (t.expire_time is null or expire_time>now()) "
    )
    List<TeamUserVo> selectAllTeamsWithUsersOptimized();

    @Select("select u.id,u.username,u.login_account,u.avatar_url,u.gender,u.phone,u.email,u.status from user_team ut\n" +
            "join user u on ut.user_id = u.id\n" +
            "where ut.team_id = #{teamId}")
    List<UserVo> selectTeamUserList(@Param("teamId") Long teamId);

    @Results(
            id = "userTeamMapperWithCondition",
            value = {
                    @Result(column = "id", property = "id", id = true),
                    @Result(column = "name", property = "name"),
                    @Result(column = "description", property = "description"),
                    @Result(column = "max_num", property = "maxNum"),
                    @Result(column = "expire_time", property = "expireTime"),
                    @Result(column = "user_id", property = "userId"),
                    @Result(column = "status", property = "status"),
                    @Result(property = "userList", javaType = List.class, many = @Many(select = "selectTeamUserList"), column = "id")
            }
    )
    @Select({
            "<script>",
            "SELECT id, name, description, max_num, expire_time, user_id, status, owner_user_id",
            "FROM team t",
            "WHERE t.is_delete = 0 and (t.expire_time is null or expire_time>now()) ",
            "<if test='id != null'> AND id = #{id}</if>",
            "<if test='name != null and name.trim() != \"\"'> AND name LIKE CONCAT('%', #{name}, '%')</if>",
            "<if test='description != null and description.trim() != \"\"'> AND description LIKE CONCAT('%', #{description}, '%')</if>",
            "<if test='searchText != null and searchText.trim() != \"\"'> AND (name LIKE CONCAT('%', #{searchText}, '%') or description LIKE CONCAT('%', #{searchText}, '%'))</if>",
            "<if test='maxNum != null'> AND max_num = #{maxNum}</if>",
            "<if test='userId != null'> AND user_id = #{userId}</if>",
            "<if test='ownerUserId != null'> AND owner_user_id = #{ownerUserId}</if>",
            "<if test='status != null'> AND status = #{status}</if>",
            "</script>"
    })
    List<TeamUserVo> selectTeamsWithDynamicQuery(TeamQuery query);
}




