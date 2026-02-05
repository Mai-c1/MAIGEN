package com.maigen.api.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maigen.api.entity.AiWorkflow;
import com.maigen.api.entity.AiWorkflowStep;
import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.dto.PageQuery;
import com.maigen.api.service.AiWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理端-AI工作流管理")
@RestController
@RequestMapping("/admin/workflow")
@RequiredArgsConstructor
public class ManageAiWorkflowController {

    private final AiWorkflowService workflowService;

    @Operation(summary = "获取方案列表")
    @GetMapping("/list")
    @SaCheckPermission("workflow:view")
    public SaResult list(PageQuery query) {
        Page<AiWorkflow> page = query.toMpPage();
        workflowService.page(page);
        return SaResult.data(PageDTO.of(page, w -> w));
    }

    @Operation(summary = "创建方案")
    @PostMapping("/create")
    @SaCheckPermission("workflow:add")
    public SaResult create(@RequestBody AiWorkflow workflow) {
        workflowService.save(workflow);
        return SaResult.ok("创建成功");
    }

    @Operation(summary = "更新方案")
    @PostMapping("/update")
    @SaCheckPermission("workflow:edit")
    public SaResult update(@RequestBody AiWorkflow workflow) {
        boolean success = workflowService.updateById(workflow);
        if (success) workflowService.syncCache();
        return SaResult.ok("更新成功");
    }

    @Operation(summary = "删除方案")
    @PostMapping("/delete")
    @SaCheckPermission("workflow:delete")
    public SaResult delete(@RequestParam Long id) {
        boolean success = workflowService.removeById(id);
        if (success) workflowService.syncCache();
        return SaResult.ok("删除成功");
    }

    @Operation(summary = "复制方案")
    @PostMapping("/copy")
    @SaCheckPermission("workflow:add")
    public SaResult copy(@RequestParam Long id) {
        workflowService.copyWorkflow(id);
        return SaResult.ok("复制成功");
    }

    @Operation(summary = "获取步骤列表")
    @GetMapping("/steps")
    @SaCheckPermission("workflow:view")
    public SaResult getSteps(@RequestParam Long workflowId) {
        return SaResult.data(workflowService.getSteps(workflowId));
    }

    @Operation(summary = "保存步骤列表")
    @PostMapping("/steps/save")
    @SaCheckPermission("workflow:edit")
    public SaResult saveSteps(@RequestParam Long workflowId, @RequestBody List<AiWorkflowStep> steps) {
        workflowService.saveSteps(workflowId, steps);
        return SaResult.ok("保存成功");
    }
}
