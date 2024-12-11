package com.lzhphantom.lzhphantom_findfriendsbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.Tag;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.TagService;
import com.lzhphantom.lzhphantom_findfriendsbackend.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author luozh
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2024-12-10 14:13:32
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




