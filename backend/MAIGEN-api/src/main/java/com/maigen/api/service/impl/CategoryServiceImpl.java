package com.maigen.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.Category;
import com.maigen.api.service.CategoryService;
import com.maigen.api.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

/**
* @author 25128
* @description 针对表【category】的数据库操作Service实现
* @createDate 2026-01-31 00:12:15
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

}




