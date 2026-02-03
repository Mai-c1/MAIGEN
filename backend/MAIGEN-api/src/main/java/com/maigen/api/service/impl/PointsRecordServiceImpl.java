package com.maigen.api.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.PointsRecord;
import com.maigen.api.entity.User;
import com.maigen.api.mapper.PointsRecordMapper;
import com.maigen.api.mapper.UserMapper;
import com.maigen.api.model.vo.PointsBalanceVO;
import com.maigen.api.model.vo.PointsRecordVO;
import com.maigen.api.service.PointsRecordService;
import com.maigen.common.core.constant.PointsConstants;
import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.PageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 25128
 * @description 针对表【points_record】的数据库操作Service实现
 * @createDate 2026-01-29 19:57:59
 */
@Service
@RequiredArgsConstructor
public class PointsRecordServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord>
        implements PointsRecordService {

    private final UserMapper userMapper;

    @Override
    public PointsBalanceVO getBalance() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        return PointsBalanceVO.builder()
                .balance(user.getPoints())
                .build();
    }

    @Override
    public PageDTO<PointsRecordVO> getRecordPage(PageQuery query) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<PointsRecord> page = query.toMpPage();

        lambdaQuery()
                .eq(PointsRecord::getUserId, userId)
                .orderByDesc(PointsRecord::getCreatedAt)
                .page(page);

        return PageDTO.of(page, record -> {
            PointsRecordVO vo = new PointsRecordVO();
            BeanUtil.copyProperties(record, vo);
            vo.setRelatedId(record.getRelatedId());
            vo.setCreatedAt(record.getCreatedAt());
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int adReward() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 1. 更新用户积分
        User user = userMapper.selectById(userId);
        user.setPoints(user.getPoints() + PointsConstants.AD_REWARD);
        userMapper.updateById(user);

        // 2. 记录积分流水
        PointsRecord record = PointsRecord.builder()
                .userId(userId)
                .amount(PointsConstants.AD_REWARD)
                .source(PointsConstants.SOURCE_AD_REWARD)
                .description("观看广告激励奖励")
                .createdAt(LocalDateTime.now())
                .build();
        this.save(record);

        return PointsConstants.AD_REWARD;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rewardPoints(Long userId, Integer amount, String source, String description) {
        // 1. 更新用户积分
        User user = userMapper.selectById(userId);
        if (user == null) return;
        
        user.setPoints(user.getPoints() + amount);
        userMapper.updateById(user);

        // 2. 记录积分流水
        PointsRecord record = PointsRecord.builder()
                .userId(userId)
                .amount(amount)
                .source(source)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        this.save(record);
    }
}




