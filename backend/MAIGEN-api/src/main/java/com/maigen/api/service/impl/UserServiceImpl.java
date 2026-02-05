package com.maigen.api.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.*;
import com.maigen.api.mapper.RoleMapper;
import com.maigen.api.mapper.UserMapper;
import com.maigen.api.mapper.UserRoleMapper;
import com.maigen.api.model.dto.*;
import com.maigen.api.model.dto.admin.AdminUserQueryDTO;
import com.maigen.api.model.vo.AdminUserVO;
import com.maigen.api.model.vo.TokenVO;
import com.maigen.api.model.vo.UserVO;
import com.maigen.api.service.InvitationService;
import com.maigen.api.service.PointsRecordService;
import com.maigen.api.service.UserService;
import com.maigen.common.core.constant.PointsConstants;
import com.maigen.common.core.exception.CustomException;

import com.maigen.common.email.util.EmailUtil;
import com.maigen.common.redis.constant.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final StringRedisTemplate redisTemplate;
    private final EmailUtil emailUtil;
    private final PointsRecordService recordService;
    private final InvitationService invitationService;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    @Override
    public void sendCode(String email, String type) {
        // 0. 校验邮箱格式
        if (!Validator.isEmail(email)) {
            throw new CustomException("邮箱格式不正确", 600000);
        }

        // 1. 根据类型校验
        Long count = this.count(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if ("register".equals(type)) {
            if (count > 0) {
                throw new CustomException("该邮箱已被注册", 600001);
            }
        } else if ("reset".equals(type)) {
            if (count == 0) {
                throw new CustomException("该邮箱未注册", 600002);
            }
        }

        // 2. 校验发送频率 (60秒内不允许重复发送)
        String key = RedisConstants.getAuthCodeKey(email);
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (expire != null && expire > 240) {
            throw new CustomException("请求过于频繁，请稍后再试", 600004);
        }

        // 3. 生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 4. 存入 Redis，有效期 5 分钟
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

        // 4. 发送邮件
        String subject = "MAIGEN 验证码";
        String content = "您的验证码是：" + code + "，有效期 5 分钟。";
        emailUtil.sendMsg(email, subject, content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        // 1. 校验验证码
        String key = RedisConstants.getAuthCodeKey(dto.getEmail());
        String code = redisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(code) || !code.equals(dto.getCode())) {
            throw new CustomException("验证码错误或已失效", 600003);
        }

        // 2. 校验邮箱是否重复 (二次校验)
        Long count = this.count(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (count > 0) {
            throw new CustomException("该邮箱已被注册", 600001);
        }
        if (lambdaQuery().eq(User::getUsername, dto.getEmail()).exists()) {
            throw new CustomException("用户名重复", 600001);
        }

        // 3. 创建用户
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername()); // 默认用户名为邮箱
        // 密码加密: MD5(密码 + 盐)
        user.setPassword(SaSecureUtil.md5BySalt(dto.getPassword(), "MAIGEN_SALT"));
        user.setNickname(dto.getUsername());
        user.setPoints(PointsConstants.REGISTER_REWARD); // 注册送积分
        user.setStatus(1); // 正常状态
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 生成唯一邀请码
        user.setInvitationCode(RandomUtil.randomString(8).toUpperCase());

        this.save(user);

        // 4. 处理邀请逻辑
        if (StrUtil.isNotBlank(dto.getInvitationCode())) {
            User inviter = lambdaQuery().eq(User::getInvitationCode, dto.getInvitationCode()).one();

            if (inviter != null) {
                // 4.1 记录邀请关系
                Invitation invitation = new Invitation();
                invitation.setInviterId(inviter.getId());
                invitation.setInviteeId(user.getId());
                invitation.setInvitationCode(dto.getInvitationCode());
                invitation.setStatus(1);
                invitation.setCreatedAt(LocalDateTime.now());
                invitationService.save(invitation);

                // 4.2 给邀请人加积分
                inviter.setPoints(inviter.getPoints() + PointsConstants.INVITATION_REWARD_INVITER);
                this.updateById(inviter);
                // 4.3 记录积分变动
                PointsRecord record = PointsRecord.builder()
                        .userId(inviter.getId())
                        .amount(PointsConstants.INVITATION_REWARD_INVITER)
                        .source("invitation")
                        .description("邀请新用户: " + user.getNickname())
                        .build();

                recordService.save(record);
            }
        }

        // 5. 删除验证码
        redisTemplate.delete(key);
    }

    @Override
    public TokenVO login(LoginDTO dto) {
        // 1. 根据用户名或邮箱查询用户
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
                .or()
                .eq(User::getEmail, dto.getUsername()));

        if (user == null) {
            throw new CustomException("用户不存在", 404);
        }

        // 2. 校验密码
        String inputPwd = SaSecureUtil.md5BySalt(dto.getPassword(), "MAIGEN_SALT");
        if (!inputPwd.equals(user.getPassword())) {
            throw new CustomException("密码错误", 404);
        }

        // 3. 校验状态
        if (user.getStatus() != 1) {
            throw new CustomException("账号已被禁用", 404);
        }

        // 4. 登录 (Sa-Token)
        StpUtil.login(user.getId());

        // 5. 返回 Token 信息
        return TokenVO.builder()
                .tokenName(StpUtil.getTokenName())
                .tokenValue(StpUtil.getTokenValue())
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public void forgetPassword(ForgetPasswordDTO dto) {
        // 1. 校验验证码
        String key = RedisConstants.getAuthCodeKey(dto.getEmail());
        String code = redisTemplate.opsForValue().get(key);
        if (code == null || !code.equals(dto.getCode())) {
            throw new CustomException("验证码错误或已失效", 404);
        }

        // 2. 查询用户
        User user = this.lambdaQuery().eq(User::getEmail, dto.getEmail()).one();
        if (user == null) {
            throw new CustomException("用户不存在", 404);
        }

        // 3. 重置密码
        user.setPassword(SaSecureUtil.md5BySalt(dto.getNewPassword(), "MAIGEN_SALT"));
        this.updateById(user);

        // 4. 删除验证码
        redisTemplate.delete(key);
    }

    @Override
    public void updateUserInfo(UserUpdateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = this.getById(userId);

        if (user == null) {
            throw new CustomException("用户不存在", 404);
        }

        // 仅允许更新 nickname, avatar, bio
        if (StrUtil.isNotBlank(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (StrUtil.isNotBlank(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar());
        }

        user.setUpdatedAt(LocalDateTime.now());
        this.updateById(user);
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = this.getById(userId);
        if (user == null) {
            throw new CustomException("用户不存在", 404);
        }

        // 校验旧密码
        String oldPwd = SaSecureUtil.md5BySalt(dto.getOldPassword(), "MAIGEN_SALT");
        if (!oldPwd.equals(user.getPassword())) {
            throw new CustomException("旧密码错误", 400);
        }

        // 修改密码
        String newPwd = SaSecureUtil.md5BySalt(dto.getNewPassword(), "MAIGEN_SALT");
        user.setPassword(newPwd);
        user.setUpdatedAt(LocalDateTime.now());
        this.updateById(user);
    }

    @Override
    public UserVO getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = this.getById(userId);
        if (user == null) {
            throw new CustomException("用户不存在", 404);
        }

        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        vo.setId(user.getId());
        vo.setCreatedAt(user.getCreatedAt());
        
        // 注入权限信息 (利用 Sa-Token 缓存机制)
        vo.setRoles(StpUtil.getRoleList());
        vo.setPermissions(StpUtil.getPermissionList());
        
        return vo;
    }

    @Override
    public PageDTO<AdminUserVO> getAdminUserPage(AdminUserQueryDTO query) {
        Page<User> page = query.toMpPage();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 1. 基础条件过滤
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(User::getUsername, query.getKeyword())
                    .or()
                    .like(User::getEmail, query.getKeyword()));
        }
        if (query.getStatus() != null) {
            wrapper.eq(User::getStatus, query.getStatus());
        }
        if (query.getMinPoints() != null) {
            wrapper.ge(User::getPoints, query.getMinPoints());
        }
        if (query.getMaxPoints() != null) {
            wrapper.le(User::getPoints, query.getMaxPoints());
        }

        // 2. 角色过滤 (子查询: 先查出符合角色的 userIds)
        if (StrUtil.isNotBlank(query.getRole())) {
            // 先查角色ID
            Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getName, query.getRole()));
            if (role != null) {
                // 再查关联表
                List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, role.getId()));
                if (userRoles.isEmpty()) {
                    return PageDTO.empty(); // 该角色下无用户
                }
                List<Long> userIds = userRoles.stream().map(UserRole::getUserId).collect(Collectors.toList());
                wrapper.in(User::getId, userIds);
            } else {
                return PageDTO.empty(); // 角色不存在
            }
        }

        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> userPage = this.page(page, wrapper);

        // 3. 组装 VO (填充角色信息)
        // 批量查询当前页用户的角色
        List<Long> pageUserIds = userPage.getRecords().stream().map(User::getId).collect(Collectors.toList());
        Map<Long, String> userRoleMap = new java.util.HashMap<>();
        
        if (!pageUserIds.isEmpty()) {
            // 这里简单处理：取用户的第一个角色，或者拼接多个角色
            // 复杂查询建议用 XML 联表，但既然要求 Lambda，我们在内存中组装
            List<UserRole> urs = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, pageUserIds));
            if (!urs.isEmpty()) {
                List<Long> roleIds = urs.stream().map(UserRole::getRoleId).distinct().collect(Collectors.toList());
                Map<Long, String> roleNameMap = roleMapper.selectBatchIds(roleIds).stream()
                        .collect(Collectors.toMap(Role::getId, Role::getName));
                
                for (UserRole ur : urs) {
                    // 如果一个用户有多个角色，这里覆盖了，实际应由业务决定显示主角色或列表
                    userRoleMap.put(ur.getUserId(), roleNameMap.get(ur.getRoleId()));
                }
            }
        }

        return PageDTO.of(userPage,u -> {
            AdminUserVO vo = new AdminUserVO();
            BeanUtil.copyProperties(u, vo);
            vo.setRole(userRoleMap.getOrDefault(u.getId(), "普通用户")); // 默认值可调整
            return vo;
        });
    }
}
