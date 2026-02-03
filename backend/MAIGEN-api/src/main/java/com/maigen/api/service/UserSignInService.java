package com.maigen.api.service;

import com.maigen.api.entity.UserSignIn;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maigen.api.model.vo.SignInResultVO;

import java.time.LocalDate;
import java.util.List;

/**
* @author 25128
* @description 针对表【user_sign_in】的数据库操作Service
* @createDate 2026-01-31 00:12:16
*/
public interface UserSignInService extends IService<UserSignIn> {
    /**
     * 签到
     * @param userId 用户ID
     * @return 签到结果
     */
    SignInResultVO signIn(Long userId);

    /**
     * 获取用户本月签到日期列表
     * @param userId 用户ID
     * @return 日期列表
     */
    List<LocalDate> getMonthSignInDays(Long userId);
}
