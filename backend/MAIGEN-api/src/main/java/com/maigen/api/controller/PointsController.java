package com.maigen.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.maigen.api.model.vo.PointsBalanceVO;
import com.maigen.api.model.vo.PointsRecordVO;
import com.maigen.api.service.PointsRecordService;
import com.maigen.api.service.UserSignInService;
import com.maigen.common.core.annotation.Log;
import com.maigen.common.core.annotation.RateLimit;
import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
@Tag(name = "积分管理", description = "用户积分余额查询与流水记录")
@SaCheckLogin
public class PointsController {

    private final PointsRecordService pointsRecordService;
    private final UserSignInService userSignInService;

    @GetMapping("/balance")
    @Operation(summary = "查询积分余额", description = "获取当前登录用户的积分余额")
    public SaResult getBalance() {
        return SaResult.data(pointsRecordService.getBalance());
    }

    @GetMapping("/records")
    @Operation(summary = "查询积分流水", description = "分页获取当前登录用户的积分变动明细")
    public SaResult getRecordPage(PageQuery query) {
        return SaResult.data(pointsRecordService.getRecordPage(query));
    }

    @PostMapping("/sign-in")
    @Operation(summary = "每日签到", description = "每日签到领取积分奖励")
    @Log(module = "积分管理", operation = "每日签到")
    public SaResult signIn() {
        return SaResult.data(userSignInService.signIn(StpUtil.getLoginIdAsLong()));
    }

    @GetMapping("/sign-in/month")
    @Operation(summary = "查询本月签到日期", description = "返回用户本月已签到的日期列表")
    public SaResult getMonthSignInDays() {
        return SaResult.data(userSignInService.getMonthSignInDays(StpUtil.getLoginIdAsLong()));
    }

    @PostMapping("/ad-reward")
    @Operation(summary = "广告激励", description = "观看广告奖励积分")
    @Log(module = "积分管理", operation = "广告激励奖励")
    @RateLimit(time = 3600, count = 5, message = "每小时最多通过广告获得 5 次积分")
    public SaResult adReward() {
        return SaResult.data(pointsRecordService.adReward());
    }
}
