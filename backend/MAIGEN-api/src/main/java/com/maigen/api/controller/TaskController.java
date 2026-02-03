package com.maigen.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.maigen.api.model.dto.CreateTaskDTO;
import com.maigen.api.service.TaskService;
import com.maigen.common.core.annotation.Log;
import com.maigen.common.core.annotation.RateLimit;
import com.maigen.api.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "题目生成任务的创建与状态查询")
@SaCheckLogin
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/create")
    @Operation(summary = "创建任务", description = "提交题目描述和配置，创建生成任务")
    @Log(module = "任务管理", operation = "创建任务")
    @RateLimit(time = 3600, count = 10, message = "每小时最多创建 10 个任务")
    public SaResult createTask(@RequestBody CreateTaskDTO dto) {
        return SaResult.data(taskService.createTask(dto));
    }

    @GetMapping("/strategies")
    @Operation(summary = "获取可选策略列表", description = "获取系统预设的所有测试点生成策略")
    public SaResult getStrategies() {
        return SaResult.data(taskService.getStrategies());
    }

    @GetMapping("/status/{taskId}")
    @Operation(summary = "查询任务状态", description = "根据任务ID查询当前进度和结果摘要")
    public SaResult getTaskStatus(@PathVariable Long taskId) {
        return SaResult.data(taskService.getTaskStatus(taskId));
    }

    @GetMapping("/detail/{taskId}")
    @Operation(summary = "获取任务详情", description = "获取任务的完整配置信息和结果")
    public SaResult getTaskDetail(@PathVariable Long taskId) {
        return SaResult.data(taskService.getTaskDetail(taskId));
    }

    @PostMapping("/cancel/{taskId}")
    @Operation(summary = "取消任务", description = "取消正在进行的生成任务")
    public SaResult cancelTask(@PathVariable Long taskId) {
        taskService.cancelTask(taskId);
        return SaResult.ok("任务已取消");
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "删除任务", description = "删除任务记录及相关资源")
    public SaResult deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return SaResult.ok("任务已删除");
    }

    @GetMapping("/list")
    @Operation(summary = "获取任务列表", description = "分页查询当前用户的任务列表")
    public SaResult getTaskList(PageQuery query) {
        return SaResult.data(taskService.getTaskList(query));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取任务统计", description = "获取当前用户的任务状态统计数据")
    public SaResult getTaskStatistics() {
        return SaResult.data(taskService.getTaskStatistics());
    }

    @PostMapping("/retry/{taskId}")
    @Operation(summary = "重试任务", description = "对执行失败的任务发起重新生成")
    public SaResult retryTask(@PathVariable Long taskId) {
        taskService.retryTask(taskId);
        return SaResult.ok("已发起重试");
    }
}
