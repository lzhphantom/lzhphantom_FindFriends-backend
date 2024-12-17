package com.lzhphantom.lzhphantom_findfriendsbackend.model.request.team;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 379687155428265179L;
    private Long id;


    /**
     * 密码
     */
    private String password;

}
