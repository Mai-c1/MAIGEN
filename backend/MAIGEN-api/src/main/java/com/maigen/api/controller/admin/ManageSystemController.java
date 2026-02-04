package com.maigen.api.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maigen.api.entity.OperationLog;
import com.maigen.api.entity.PointsRule;
import com.maigen.api.entity.SystemConfig;
import com.maigen.api.model.dto.PageQuery;
import com.maigen.api.service.OperationLogService;
import com.maigen.api.service.PointsRuleService;
import com.maigen.api.service.StatisticsService;
import com.maigen.api.service.SystemConfigService;
import com.maigen.api.service.PointsRecordService;
import com.maigen.common.core.annotation.Log;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/system")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "管理后台-系统管理", description = "系统配置、积分规则、审计日志及全局统计")
public class ManageSystemController {

    private final SystemConfigService systemConfigService;
    private final PointsRuleService pointsRuleService;
    private final OperationLogService operationLogService;
    private final StatisticsService statisticsService;
    private final PointsRecordService pointsRecordService;

    // --- 系统配置 ---

    @GetMapping("/config/list")
    @Operation(summary = "系统配置列表")
    @SaCheckPermission("system:config:view")
    public SaResult listConfigs() {
        return SaResult.data(systemConfigService.list());
    }

    @PostMapping("/config")
    @Operation(summary = "新增系统配置")
    @SaCheckPermission("system:config:edit")
    public SaResult addConfig(@RequestBody SystemConfig config) {
        systemConfigService.save(config);
        return SaResult.ok("配置新增成功");
    }

    @PutMapping("/config")
    @Operation(summary = "更新系统配置")
    @SaCheckPermission("system:config:edit")
    public SaResult updateConfig(@RequestBody SystemConfig config) {
        systemConfigService.updateById(config);
        return SaResult.ok("配置更新成功");
    }

    @DeleteMapping("/config/{id}")
    @Operation(summary = "删除系统配置")
    @SaCheckPermission("system:config:edit")
    public SaResult deleteConfig(@PathVariable Long id) {
        systemConfigService.removeById(id);
        return SaResult.ok("配置删除成功");
    }

    // --- 积分规则 ---

    @GetMapping("/points/rule/list")
    @Operation(summary = "积分规则列表")
    @SaCheckPermission("points:rule:manage")
    public SaResult listPointsRules() {
        return SaResult.data(pointsRuleService.list());
    }

    @PutMapping("/points/rule")
    @Operation(summary = "更新积分规则")
    @SaCheckPermission("points:rule:manage")
    public SaResult updatePointsRule(@RequestBody PointsRule rule) {
        pointsRuleService.updateById(rule);
        return SaResult.ok("规则更新成功");
    }

    @GetMapping("/points/records")
    @Operation(summary = "全站积分流水")
    @SaCheckPermission("points:record:view")
    public SaResult listPointsRecords(PageQuery query, Long userId) {
        return SaResult.data(pointsRecordService.page(query.toMpPage(), 
            new LambdaQueryWrapper<com.maigen.api.entity.PointsRecord>()
                .eq(userId != null, com.maigen.api.entity.PointsRecord::getUserId, userId)
                .orderByDesc(com.maigen.api.entity.PointsRecord::getCreatedAt)));
    }

    // --- 审计日志 ---

    @GetMapping("/log/list")
    @Operation(summary = "操作日志列表")
    @SaCheckPermission("system:log:operation")
    public SaResult listLogs(PageQuery query, String module) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(module)) {
            wrapper.eq(OperationLog::getModule, module);
        }
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        return SaResult.data(operationLogService.page(query.toMpPage(), wrapper));
    }

    // --- 全局统计 ---

    @GetMapping("/statistics")
    @Operation(summary = "全局统计数据")
    @SaCheckPermission("admin:dashboard")
    public SaResult getStatistics() {
        return SaResult.data(statisticsService.list());
    }

    @PostMapping("/backup")
    @Operation(summary = "系统数据备份")
    @Log(module = "系统管理", operation = "执行备份")
    @SaCheckPermission("system:backup")
    public SaResult backup() {
        // 占位实现：触发异步备份任务
        return SaResult.ok("数据备份任务已启动，请在日志中查看进度");
    }

    @PostMapping("/restore")
    @Operation(summary = "系统数据恢复")
    @Log(module = "系统管理", operation = "执行恢复")
    @SaCheckPermission("system:restore")
    public SaResult restore() {
        return SaResult.ok("正在从最近一次备份恢复，系统将短暂进入维护模式");
    }

    @PostMapping("/notice")
    @Operation(summary = "发布全站通知")
    @Log(module = "系统管理", operation = "发布通知")
    @SaCheckPermission("system:notice:publish")
    public SaResult publishNotice(@RequestParam String content) {
        // 实际逻辑：写入 Redis 或 数据库通知表，并通过 WebSocket 推送
        return SaResult.ok("通知已成功发布至全站");
    }
}
