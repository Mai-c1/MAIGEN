package com.maigen.api.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maigen.api.entity.Category;
import com.maigen.api.entity.CommunityContent;
import com.maigen.api.entity.Tag;
import com.maigen.api.model.dto.PageQuery;
import com.maigen.api.model.dto.admin.AuditDTO;
import com.maigen.api.service.CategoryService;
import com.maigen.api.service.CommunityContentService;
import com.maigen.api.service.PointsRecordService;
import com.maigen.api.service.TagService;
import com.maigen.common.core.annotation.Log;
import com.maigen.common.core.constant.PointsConstants;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/content")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "管理后台-内容管理", description = "社区资源审核、分类及标签管理")
@SaCheckRole("管理员")
public class ManageContentController {

    private final CommunityContentService communityContentService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final PointsRecordService pointsRecordService;

    // --- 社区内容管理 ---

    @GetMapping("/list")
    @Operation(summary = "社区内容列表")
    @SaCheckPermission("community:view")
    public SaResult listContent(PageQuery query, String keyword, Integer status) {
        LambdaQueryWrapper<CommunityContent> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(CommunityContent::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(CommunityContent::getStatus, status);
        }
        return SaResult.data(communityContentService.page(query.toMpPage(), wrapper));
    }

    @PutMapping("/{id}/audit")
    @Operation(summary = "内容审核")
    @Log(module = "内容管理", operation = "审核内容")
    @SaCheckPermission("community:approve")
    public SaResult auditContent(@PathVariable Long id, @RequestBody AuditDTO dto) {
        CommunityContent content = communityContentService.getById(id);
        if (content == null) return SaResult.error("内容不存在");
        
        // 状态映射: PENDING=0, PASS=1, REJECT=2
        int status = "PASS".equals(dto.getStatus()) ? 1 : 2;
        content.setStatus(status);
        communityContentService.updateById(content);
        
        // 如果审核通过，发放积分奖励给作者
        if (status == 1) {
            pointsRecordService.rewardPoints(
                content.getUserId(), 
                PointsConstants.COMMUNITY_SHARE_REWARD, 
                PointsConstants.SOURCE_COMMUNITY_SHARE, 
                "社区资源分享审核通过奖励: " + content.getTitle()
            );
        }
        
        return SaResult.ok("审核操作成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除内容")
    @Log(module = "内容管理", operation = "删除内容")
    @SaCheckPermission("community:delete")
    public SaResult deleteContent(@PathVariable Long id) {
        communityContentService.removeById(id);
        return SaResult.ok("删除成功");
    }

    // --- 分类管理 ---

    @GetMapping("/category/list")
    @Operation(summary = "分类列表")
    @SaCheckPermission("community:tag:manage")
    public SaResult listCategories() {
        return SaResult.data(categoryService.list());
    }

    @PostMapping("/category")
    @Operation(summary = "新增分类")
    @SaCheckPermission("community:tag:manage")
    public SaResult createCategory(@RequestBody Category category) {
        categoryService.save(category);
        return SaResult.ok("创建成功");
    }

    @PutMapping("/category")
    @Operation(summary = "更新分类")
    @SaCheckPermission("community:tag:manage")
    public SaResult updateCategory(@RequestBody Category category) {
        categoryService.updateById(category);
        return SaResult.ok("更新成功");
    }

    @DeleteMapping("/category/{id}")
    @Operation(summary = "删除分类")
    @SaCheckPermission("community:tag:manage")
    public SaResult deleteCategory(@PathVariable Long id) {
        categoryService.removeById(id);
        return SaResult.ok("删除成功");
    }

    // --- 标签管理 ---

    @GetMapping("/tag/list")
    @Operation(summary = "标签列表")
    @SaCheckPermission("community:tag:manage")
    public SaResult listTags() {
        return SaResult.data(tagService.list());
    }

    @PostMapping("/tag")
    @Operation(summary = "新增标签")
    @SaCheckPermission("community:tag:manage")
    public SaResult createTag(@RequestBody Tag tag) {
        tagService.save(tag);
        return SaResult.ok("创建成功");
    }

    @DeleteMapping("/tag/{id}")
    @Operation(summary = "删除标签")
    @SaCheckPermission("community:tag:manage")
    public SaResult deleteTag(@PathVariable Long id) {
        tagService.removeById(id);
        return SaResult.ok("删除成功");
    }
}
