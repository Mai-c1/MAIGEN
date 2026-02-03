package com.maigen.api.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maigen.api.entity.*;
import com.maigen.api.mapper.CommunityContentMapper;
import com.maigen.api.mapper.CommunityUnlockMapper;
import com.maigen.api.mapper.UserMapper;
import com.maigen.api.model.dto.*;
import com.maigen.api.model.vo.CommunityDetailVO;
import com.maigen.api.model.vo.CommunityVO;
import com.maigen.api.service.*;
import com.maigen.common.core.constant.PointsConstants;
import com.maigen.common.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 25128
 * @description 针对表【community_content】的数据库操作Service实现
 * @createDate 2026-01-29 19:57:59
 */
@Service
@RequiredArgsConstructor
public class CommunityContentServiceImpl extends ServiceImpl<CommunityContentMapper, CommunityContent>
        implements CommunityContentService {

    private final UserMapper userMapper;
    private final CommunityUnlockMapper communityUnlockMapper;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final CommunityTagService communityTagService;
    private final CommunityLikeService communityLikeService;
    private final CommunityRatingService communityRatingService;
    private final PointsRecordService pointsRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long share(CommunityShareDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 1. 保存内容
        CommunityContent content = new CommunityContent();
        BeanUtil.copyProperties(dto, content);
        content.setUserId(userId);
        content.setCreatedAt(LocalDateTime.now());
        content.setUpdatedAt(LocalDateTime.now());
        content.setStatus(0); // 待审核
        content.setViewCount(0);
        content.setDownloadCount(0);
        content.setLikeCount(0);
        content.setRatingAvg(0.0);
        content.setRatingCount(0);
        this.save(content);

        // 2. 保存标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            List<CommunityTag> tags = dto.getTagIds().stream()
                    .map(tagId -> CommunityTag.builder()
                            .communityId(content.getId())
                            .tagId(tagId)
                            .build())
                    .collect(Collectors.toList());
            communityTagService.saveBatch(tags);
        }

        return content.getId();
    }

    @Override
    public PageDTO<CommunityVO> getPage(CommunityQuery query) {
        Page<CommunityContent> page = query.toMpPage();
        LambdaQueryWrapper<CommunityContent> wrapper = new LambdaQueryWrapper<>();

        // 筛选
        wrapper.eq(CommunityContent::getStatus, 1); // 仅审核通过的
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.like(CommunityContent::getTitle, query.getKeyword());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(CommunityContent::getCategoryId, query.getCategoryId());
        }

        // 排序
        if ("popular_download".equals(query.getOrderBy())) {
            wrapper.orderByDesc(CommunityContent::getDownloadCount);
        } else if ("popular_like".equals(query.getOrderBy())) {
            wrapper.orderByDesc(CommunityContent::getLikeCount);
        } else {
            wrapper.orderByDesc(CommunityContent::getCreatedAt);
        }

        this.page(page, wrapper);

        return PageDTO.of(page, content -> {
            CommunityVO vo = CommunityVO.builder().build();
            BeanUtil.copyProperties(content, vo);
            
            // 获取作者
            User author = userMapper.selectById(content.getUserId());
            vo.setAuthorName(author != null ? author.getNickname() : "未知");
            
            // 获取分类
            if (content.getCategoryId() != null) {
                Category category = categoryService.getById(content.getCategoryId());
                vo.setCategoryName(category != null ? category.getName() : null);
            }
            
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityDetailVO getDetail(Long id) {
        CommunityContent content = this.getById(id);
        if (content == null) {
            throw new CustomException("内容不存在", 404);
        }

        // 更新浏览量
        content.setViewCount(content.getViewCount() + 1);
        this.updateById(content);

        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;

        CommunityDetailVO vo = CommunityDetailVO.builder().build();
        BeanUtil.copyProperties(content, vo);

        // 获取作者
        User author = userMapper.selectById(content.getUserId());
        if (author != null) {
            vo.setAuthorName(author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }

        // 获取分类
        if (content.getCategoryId() != null) {
            Category category = categoryService.getById(content.getCategoryId());
            vo.setCategoryName(category != null ? category.getName() : null);
        }

        // 获取当前用户状态
        if (userId != null) {
            vo.setIsLiked(communityLikeService.lambdaQuery()
                    .eq(CommunityLike::getUserId, userId)
                    .eq(CommunityLike::getCommunityId, id)
                    .exists());
            vo.setIsUnlocked(communityUnlockMapper.selectCount(new LambdaQueryWrapper<CommunityUnlock>()
                    .eq(CommunityUnlock::getUserId, userId)
                    .eq(CommunityUnlock::getCommunityId, id)) > 0);
        } else {
            vo.setIsLiked(false);
            vo.setIsUnlocked(false);
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String download(Long contentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        CommunityContent content = this.getById(contentId);
        if (content == null) {
            throw new CustomException("资源不存在", 404);
        }

        // 1. 检查是否已解锁
        Long unlockCount = communityUnlockMapper.selectCount(new LambdaQueryWrapper<CommunityUnlock>()
                .eq(CommunityUnlock::getUserId, userId)
                .eq(CommunityUnlock::getCommunityId, contentId));

        if (unlockCount == 0) {
            // 2. 未解锁，检查积分
            User user = userMapper.selectById(userId);
            if (user.getPoints() < content.getPoints()) {
                throw new CustomException("积分不足，需要 " + content.getPoints() + " 积分", 400);
            }

            // 3. 扣减积分
            user.setPoints(user.getPoints() - content.getPoints());
            userMapper.updateById(user);
            
            // 记录流水
            pointsRecordService.save(PointsRecord.builder()
                    .userId(userId)
                    .amount(-content.getPoints())
                    .source("COMMUNITY_DOWNLOAD")
                    .relatedId(contentId.toString())
                    .description("下载社区资源: " + content.getTitle())
                    .createdAt(LocalDateTime.now())
                    .build());

            // 4. 增加作者积分
            User author = userMapper.selectById(content.getUserId());
            if (author != null) {
                int reward = PointsConstants.DOWNLOAD_REWARD_AUTHOR;
                author.setPoints(author.getPoints() + reward);
                userMapper.updateById(author);
                
                // 记录作者流水
                pointsRecordService.save(PointsRecord.builder()
                        .userId(author.getId())
                        .amount(reward)
                        .source("COMMUNITY_REWARD")
                        .relatedId(contentId.toString())
                        .description("资源被下载奖励: " + content.getTitle())
                        .createdAt(LocalDateTime.now())
                        .build());
            }

            // 5. 记录解锁信息
            CommunityUnlock unlock = new CommunityUnlock();
            unlock.setUserId(userId);
            unlock.setCommunityId(contentId);
            unlock.setUnlockAt(LocalDateTime.now());
            communityUnlockMapper.insert(unlock);
        }

        // 6. 更新下载计数
        content.setDownloadCount(content.getDownloadCount() + 1);
        this.updateById(content);

        return content.getDataFilePath();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        CommunityContent content = this.getById(id);
        if (content == null) throw new CustomException("内容不存在", 404);

        CommunityLike existing = communityLikeService.getOne(new LambdaQueryWrapper<CommunityLike>()
                .eq(CommunityLike::getUserId, userId)
                .eq(CommunityLike::getCommunityId, id));

        if (existing != null) {
            // 取消点赞
            communityLikeService.removeById(existing.getId());
            content.setLikeCount(Math.max(0, content.getLikeCount() - 1));
        } else {
            // 点赞
            communityLikeService.save(CommunityLike.builder()
                    .userId(userId)
                    .communityId(id)
                    .createdAt(LocalDateTime.now())
                    .build());
            content.setLikeCount(content.getLikeCount() + 1);
        }
        this.updateById(content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rate(CommunityRatingDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        CommunityContent content = this.getById(dto.getCommunityId());
        if (content == null) throw new CustomException("内容不存在", 404);

        CommunityRating existing = communityRatingService.getOne(new LambdaQueryWrapper<CommunityRating>()
                .eq(CommunityRating::getUserId, userId)
                .eq(CommunityRating::getCommunityId, dto.getCommunityId()));

        if (existing != null) {
            throw new CustomException("您已经评过分了", 400);
        }

        // 1. 保存评分
        communityRatingService.save(CommunityRating.builder()
                .userId(userId)
                .communityId(dto.getCommunityId())
                .score(dto.getScore())
                .createdAt(LocalDateTime.now())
                .build());

        // 2. 重新计算平均分
        // 简单计算：(原总分 + 新分) / (原人数 + 1)
        double totalScore = content.getRatingAvg() * content.getRatingCount();
        content.setRatingCount(content.getRatingCount() + 1);
        content.setRatingAvg((totalScore + dto.getScore()) / content.getRatingCount());
        
        this.updateById(content);
    }
}




