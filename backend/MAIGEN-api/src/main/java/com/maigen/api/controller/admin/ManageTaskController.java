package com.maigen.api.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maigen.api.entity.Task;
import com.maigen.api.entity.TaskStrategy;
import com.maigen.api.model.dto.PageQuery;
import com.maigen.api.service.TaskService;
import com.maigen.api.service.TaskStrategyService;
import com.maigen.common.core.annotation.Log;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/task")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "管理后台-任务管理", description = "全站任务监控、强制终止及生成策略管理")
@SaCheckRole("管理员")
public class ManageTaskController {

    private final TaskService taskService;
    private final TaskStrategyService taskStrategyService;

    @GetMapping("/list")
    @Operation(summary = "全站任务列表")
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
    public SaResult deleteTask(@PathVariable Long id) {
        taskService.removeById(id);
        return SaResult.ok("任务删除成功");
    }

    @GetMapping("/strategy/list")
    @Operation(summary = "生成策略列表")
    public SaResult listStrategies() {
        return SaResult.data(taskStrategyService.list());
    }

    @PostMapping("/strategy")
    @Operation(summary = "新增生成策略")
    public SaResult createStrategy(@RequestBody TaskStrategy strategy) {
        taskStrategyService.save(strategy);
        return SaResult.ok("创建成功");
    }

    @PutMapping("/strategy")
    @Operation(summary = "更新生成策略")
    public SaResult updateStrategy(@RequestBody TaskStrategy strategy) {
        taskStrategyService.updateById(strategy);
        return SaResult.ok("更新成功");
    }

    @DeleteMapping("/strategy/{id}")
    @Operation(summary = "删除生成策略")
    public SaResult deleteStrategy(@PathVariable Long id) {
        taskStrategyService.removeById(id);
        return SaResult.ok("删除成功");
    }
}
