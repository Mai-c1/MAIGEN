package com.maigen.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.Tag;
import com.maigen.api.service.TagService;
import com.maigen.api.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author 25128
* @description 针对表【tag】的数据库操作Service实现
* @createDate 2026-01-31 00:12:15
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




