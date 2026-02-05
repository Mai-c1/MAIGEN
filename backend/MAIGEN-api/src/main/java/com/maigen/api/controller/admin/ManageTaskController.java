package com.maigen.api.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maigen.api.entity.Task;

import com.maigen.api.model.dto.PageQuery;
import com.maigen.api.service.TaskService;

import com.maigen.common.core.annotation.Log;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/task")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "管理后台-任务管理", description = "全站任务监控、强制终止及生成策略管理")
public class ManageTaskController {

    private final TaskService taskService;

    @GetMapping("/list")
    @Operation(summary = "全站任务列表")
    @SaCheckPermission("task:view")
    public SaResult listTasks(PageQuery query, String keyword, Integer status) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Task::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        wrapper.orderByDesc(Task::getCreatedAt);
        return SaResult.data(taskService.page(query.toMpPage(), wrapper));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务")
    @Log(module = "任务管理", operation = "删除任务")
    @SaCheckPermission("task:delete")
    public SaResult deleteTask(@PathVariable Long id) {
        taskService.removeById(id);
        return SaResult.ok("任务删除成功");
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "强制取消任务")
    @Log(module = "任务管理", operation = "取消任务")
    @SaCheckPermission("task:cancel")
    public SaResult cancelTask(@PathVariable Long id) {
        taskService.cancelTask(id);
        return SaResult.ok("任务已强制取消");
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "重试失败任务")
    @Log(module = "任务管理", operation = "重试任务")
    @SaCheckPermission("task:retry")
    public SaResult retryTask(@PathVariable Long id) {
        taskService.retryTask(id);
        return SaResult.ok("重试指令已发出");
    }

    @GetMapping("/{id}/log")
    @Operation(summary = "查看任务日志")
    @SaCheckPermission("task:log:view")
    public SaResult getTaskLog(@PathVariable Long id) {
        // 实际逻辑应从存储或实时日志服务获取
        return SaResult.data(taskService.getById(id).getErrorMessage());
    }

    @GetMapping("/{id}/resource")
    @Operation(summary = "查看任务资源占用")
    @SaCheckPermission("task:resource:view")
    public SaResult getTaskResource(@PathVariable Long id) {
        // 占位实现，返回模拟资源数据
        return SaResult.ok("CPU: 1.2%, Memory: 256MB, Sandbox: Go-Judge");
    }


}
