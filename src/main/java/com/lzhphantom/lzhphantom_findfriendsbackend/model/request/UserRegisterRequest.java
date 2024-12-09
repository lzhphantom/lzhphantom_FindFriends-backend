package com.lzhphantom.lzhphantom_findfriendsbackend.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
