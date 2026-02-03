package com.maigen.api.service;

import com.maigen.api.entity.PointsRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 25128
* @description 针对表【points_record】的数据库操作Service
* @createDate 2026-01-29 19:57:59
*/

import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.PageQuery;
import com.maigen.api.model.vo.PointsBalanceVO;
import com.maigen.api.model.vo.PointsRecordVO;

public interface PointsRecordService extends IService<PointsRecord> {

    /**
     * 获取当前登录用户的积分余额
     * @return 余额VO
     */
    PointsBalanceVO getBalance();

    /**
     * 分页查询当前用户的积分流水
     * @param query 分页参数
     * @return 分页结果
     */
    PageDTO<PointsRecordVO> getRecordPage(PageQuery query);

    /**
     * 广告激励奖励
     * @return 奖励积分
     */
    int adReward();

    /**
     * 积分奖励/调整核心方法
     * @param userId 用户ID
     * @param amount 变动金额
     * @param source 来源
     * @param description 描述
     */
    void rewardPoints(Long userId, Integer amount, String source, String description);
}
