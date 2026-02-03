package com.maigen.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.maigen.api.model.dto.CommunityQuery;
import com.maigen.api.model.dto.CommunityRatingDTO;
import com.maigen.api.model.dto.CommunityShareDTO;
import com.maigen.api.service.CommunityContentService;
import com.maigen.common.core.annotation.Log;
import com.maigen.common.core.annotation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
@Tag(name = "社区管理", description = "社区资源的浏览与下载")
@SaCheckLogin
public class CommunityController {

    private final CommunityContentService communityContentService;

    @PostMapping("/share")
    @Operation(summary = "分享内容", description = "分享题目描述及生成的数据文件到社区")
    @Log(module = "社区管理", operation = "分享内容")
    @RateLimit(time = 3600, count = 3, message = "每小时最多分享 3 次内容")
    public SaResult share(@RequestBody CommunityShareDTO dto) {
        return SaResult.data(communityContentService.share(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "内容列表", description = "分页查询社区分享的内容")
    public SaResult getList(CommunityQuery query) {
        return SaResult.data(communityContentService.getPage(query));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "内容详情", description = "查看社区内容的详细信息")
    @Log(module = "社区管理", operation = "查看详情")
    public SaResult getDetail(@PathVariable Long id) {
        return SaResult.data(communityContentService.getDetail(id));
    }

    @PostMapping("/{contentId}/download")
    @Operation(summary = "下载资源", description = "消耗积分下载社区资源，返回下载链接")
    @Log(module = "社区管理", operation = "下载资源")
    public SaResult download(@PathVariable Long contentId) {
        String downloadUrl = communityContentService.download(contentId);
        return SaResult.data(downloadUrl);
    }

    @PostMapping("/like/{id}")
    @Operation(summary = "点赞/取消点赞", description = "对社区内容进行点赞或取消点赞")
    @RateLimit(time = 60, count = 10, message = "操作太快了，请休息一下")
    public SaResult like(@PathVariable Long id) {
        communityContentService.like(id);
        return SaResult.ok();
    }

    @PostMapping("/rate")
    @Operation(summary = "评分", description = "对社区内容进行评分 (1-5分)")
    @Log(module = "社区管理", operation = "内容评分")
    @RateLimit(time = 60, count = 5, message = "操作太快了，请休息一下")
    public SaResult rate(@RequestBody CommunityRatingDTO dto) {
        communityContentService.rate(dto);
        return SaResult.ok();
    }
}
