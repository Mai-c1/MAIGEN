package com.maigen.api.service;

import com.maigen.api.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 25128
* @description 针对表【user】的数据库操作Service
* @createDate 2026-01-29 19:57:59
*/
public interface UserService extends IService<User> {

    /**
     * 发送验证码
     */
    void sendCode(String email, String type);

    /**
     * 注册
     */
    void register(com.maigen.api.model.dto.RegisterDTO dto);

    /**
     * 登录
     */
    com.maigen.api.model.vo.TokenVO login(com.maigen.api.model.dto.LoginDTO dto);

    void forgetPassword(com.maigen.api.model.dto.ForgetPasswordDTO dto);

    void updateUserInfo(com.maigen.api.model.dto.UserUpdateDTO dto);

    /**
     * 修改密码
     */
    void changePassword(com.maigen.api.model.dto.ChangePasswordDTO dto);

    com.maigen.api.model.vo.UserVO getUserInfo();
}
