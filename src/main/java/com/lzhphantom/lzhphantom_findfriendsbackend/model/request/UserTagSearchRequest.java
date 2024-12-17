package com.lzhphantom.lzhphantom_findfriendsbackend.model.request;

import com.lzhphantom.lzhphantom_findfriendsbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserTagSearchRequest extends PageRequest implements Serializable {
    private List<String> tagList;
}
