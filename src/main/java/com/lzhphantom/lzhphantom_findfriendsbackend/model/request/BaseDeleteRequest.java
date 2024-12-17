package com.lzhphantom.lzhphantom_findfriendsbackend.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseDeleteRequest implements Serializable {

    private static final long serialVersionUID = 8411220840639584782L;
    private Long id;
}
