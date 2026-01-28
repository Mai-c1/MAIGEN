package com.maigen.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.User;
import com.maigen.api.service.UserService;
import com.maigen.api.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 25128
* @description 针对表【user】的数据库操作Service实现
* @createDate 2026-01-28 19:33:30
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




