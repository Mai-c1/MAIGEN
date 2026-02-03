package com.maigen.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.PointsRecord;
import com.maigen.api.entity.User;
import com.maigen.api.entity.UserSignIn;
import com.maigen.api.mapper.PointsRecordMapper;
import com.maigen.api.mapper.UserMapper;
import com.maigen.api.mapper.UserSignInMapper;
import com.maigen.api.model.vo.SignInResultVO;
import com.maigen.api.service.UserSignInService;
import com.maigen.common.core.constant.PointsConstants;
import com.maigen.common.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
* @author 25128
* @description 针对表【user_sign_in】的数据库操作Service实现
* @createDate 2026-01-31 00:12:16
*/
@Service
@RequiredArgsConstructor
public class UserSignInServiceImpl extends ServiceImpl<UserSignInMapper, UserSignIn>
    implements UserSignInService{

    private final UserMapper userMapper;
    private final PointsRecordMapper pointsRecordMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String SIGN_IN_BITMAP_KEY = "sign_in:status:%d:%s";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignInResultVO signIn(Long userId) {
        LocalDate today = LocalDate.now();
        String month = today.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String redisKey = String.format(SIGN_IN_BITMAP_KEY, userId, month);
        int dayOfMonth = today.getDayOfMonth();
        long offset = dayOfMonth - 1;

        // 1. Redis Bitmap 校验（解决零点跨天问题）
        Boolean alreadySignedIn = redisTemplate.opsForValue().getBit(redisKey, offset);
        if (Boolean.TRUE.equals(alreadySignedIn)) {
            throw new CustomException("今天已经签到过了", 429);
        }

        // 2. 计算奖励积分（简化逻辑：固定 5 积分）
        int rewardPoints = PointsConstants.SIGN_IN_REWARD;

        // 3. 保存签到记录到 MySQL (用于审计)
        UserSignIn signInRecord = new UserSignIn();
        signInRecord.setUserId(userId);
        signInRecord.setSignInDate(today);
        signInRecord.setPointsReward(rewardPoints);
        signInRecord.setCreatedAt(LocalDateTime.now());
        this.save(signInRecord);

        // 4. 更新用户积分余额
        User user = userMapper.selectById(userId);
        user.setPoints(user.getPoints() + rewardPoints);
        userMapper.updateById(user);

        // 5. 记录积分流水
        PointsRecord pointsRecord = PointsRecord.builder()
                .userId(userId)
                .amount(rewardPoints)
                .source(PointsConstants.SOURCE_SIGN_IN)
                .description("每日签到奖励")
                .createdAt(LocalDateTime.now())
                .build();
        pointsRecordMapper.insert(pointsRecord);

        // 6. 更新 Redis Bitmap 状态
        redisTemplate.opsForValue().setBit(redisKey, offset, true);

        // 7. 返回结果
        return SignInResultVO.builder()
                .rewardPoints(rewardPoints)
                .totalPoints(user.getPoints())
                .build();
    }

    @Override
    public List<LocalDate> getMonthSignInDays(Long userId) {
        LocalDate today = LocalDate.now();
        String monthStr = today.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String redisKey = String.format(SIGN_IN_BITMAP_KEY, userId, monthStr);
        
        List<LocalDate> signedDays = new ArrayList<>();
        int daysInMonth = today.lengthOfMonth();

        // 遍历本月所有天数（Bitmap 查询效率很高）
        for (int i = 0; i < daysInMonth; i++) {
            if (Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(redisKey, i))) {
                signedDays.add(today.withDayOfMonth(i + 1));
            }
        }

        return signedDays;
    }
}




