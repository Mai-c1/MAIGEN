package com.maigen.sandbox.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.maigen.common.core.model.dto.TaskSubmitDTO;
import com.maigen.common.miniIO.util.MiniIOUtil;
import com.maigen.common.redis.constant.RedisConstants;
import com.maigen.sandbox.client.GoJudgeClient;
import com.maigen.sandbox.config.SandboxProperties;
import com.maigen.sandbox.model.dto.GoJudgeRequest;
import com.maigen.sandbox.model.dto.GoJudgeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskExecuteService {

    private final GoJudgeClient goJudgeClient;
    private final StringRedisTemplate redisTemplate;
    private final MiniIOUtil miniIOUtil;
    private final SandboxProperties sandboxProperties;

    @PostConstruct
    public void init() {
        String defaultKey = RedisConstants.getSystemConfigKey(DEFAULT_RUN_SH_CONFIG_CODE);
        // 如果 Redis 中没有默认配置，则将代码中的默认值写入 Redis
        if (Boolean.FALSE.equals(redisTemplate.hasKey(defaultKey))) {
            log.info("初始化 Redis 中的默认 run.sh 模板: {}", DEFAULT_RUN_SH_CONFIG_CODE);
            redisTemplate.opsForValue().set(defaultKey, DEFAULT_RUN_SH);
        }
    }

    private static final String RUN_SH_CONFIG_CODE = "SANDBOX_RUN_SH_TEMPLATE";
    private static final String DEFAULT_RUN_SH_CONFIG_CODE = "SANDBOX_DEFAULT_RUN_SH";
    private static final String DEFAULT_RUN_SH = """
            #!/bin/bash
            set -e
            
            # 创建数据目录
            mkdir -p data
            
            # 1. 编译标程
            g++ std.cpp -o std.exe -O2
            
            # 2. 运行生成脚本
            python3 gen.py
            
            # 3. 运行标程生成输出文件
            for in_file in data/*.in; do
                if [ -f "$in_file" ]; then
                    out_file="${in_file%%.in}.out"
                    ./std.exe < "$in_file" > "$out_file"
                fi
            done
            
            # 4. 打包结果 (-j 表示不包含目录结构)
            zip -j "/output/data_%s.zip" data/*
            """;

    /**
     * 获取 run.sh 模板
     * 优先级：
     * 1. Redis: SANDBOX_RUN_SH_TEMPLATE (用户自定义模板)
     * 2. Redis: SANDBOX_DEFAULT_RUN_SH (系统默认模板)
     * 3. 代码兜底: DEFAULT_RUN_SH
     */
    private String getRunShTemplate() {
        // 1. 尝试从 Redis 获取自定义模板
        String customCacheKey = RedisConstants.getSystemConfigKey(RUN_SH_CONFIG_CODE);
        String template = redisTemplate.opsForValue().get(customCacheKey);
        if (StrUtil.isNotBlank(template)) {
            return template;
        }

        // 2. 尝试从 Redis 获取系统默认模板
        String defaultCacheKey = RedisConstants.getSystemConfigKey(DEFAULT_RUN_SH_CONFIG_CODE);
        template = redisTemplate.opsForValue().get(defaultCacheKey);
        if (StrUtil.isNotBlank(template)) {
            return template;
        }

        log.warn("未在 Redis 缓存中找到 run.sh 模板配置 (code: {} 或 {}), 使用代码兜底默认值", 
                RUN_SH_CONFIG_CODE, DEFAULT_RUN_SH_CONFIG_CODE);
        // 3. 使用代码兜底
        return DEFAULT_RUN_SH;
    }

    /**
     * 执行任务
     */
    public com.maigen.common.core.model.dto.TaskResultDTO executeTask(Long taskId) throws Exception {
        // 1. 获取任务数据
        String dataKey = RedisConstants.getTaskDataKey(taskId);
        String jsonData = redisTemplate.opsForValue().get(dataKey);
        TaskSubmitDTO taskData = JSONUtil.toBean(jsonData, TaskSubmitDTO.class);

        // 2. 获取 AI 生成的代码
        String codeKey = RedisConstants.getTaskCodeKey(taskId);
        String aiJson = redisTemplate.opsForValue().get(codeKey);
        String genPy;
        try {
            genPy = JSONUtil.parseObj(aiJson).getStr("code");
        } catch (Exception e) {
            // 如果不是 JSON，则尝试直接作为代码处理
            genPy = aiJson;
        }

        if (StrUtil.isBlank(genPy)) {
            throw new RuntimeException("AI 生成代码为空,请联系管理员，任务ID：" + taskId);
        }

        // 3. 构造 run.sh
        String template = getRunShTemplate();
        // 使用 String.format 注入 TASK_ID (兼容 %s)
        String runSh = String.format(template, taskId);

        // 4. 构造 go-judge 请求
        Map<String, GoJudgeRequest.FileContent> copyIn = new HashMap<>();
        copyIn.put("run.sh", GoJudgeRequest.FileContent.builder().content(runSh).build());
        copyIn.put("std.cpp", GoJudgeRequest.FileContent.builder().content(taskData.getStandardCode()).build());
        copyIn.put("gen.py", GoJudgeRequest.FileContent.builder().content(genPy).build());

        GoJudgeRequest.Cmd cmd = GoJudgeRequest.Cmd.builder()
                .args(List.of("/bin/bash", "run.sh"))
                .env(List.of("PATH=/usr/bin:/bin"))
                .files(List.of(
                        Map.of("content", ""),             // FD 0: stdin 必须是空字符串
                        Map.of("name", "stdout", "max", 10240),
                        Map.of("name", "stderr", "max", 10240)
                ))
                .cpuLimit(100000000000L) // 100s
                .memoryLimit(512 * 1024 * 1024L) // 512MB
                .procLimit(64)
                .copyIn(copyIn)
                .build();

        GoJudgeRequest request = GoJudgeRequest.builder()
                .cmd(List.of(cmd))
                .build();

        // 5. 调用沙箱
        log.info("开始调用沙箱执行任务: taskId={}", taskId);
        List<GoJudgeResponse> responses = goJudgeClient.execute(request);
        GoJudgeResponse response = responses.get(0);

        if (!"Accepted".equals(response.getStatus())) {
            String stderr = response.getFiles().get("stderr");
            log.error("沙箱执行失败: taskId={}, status={}, stderr={}", taskId, response.getStatus(), stderr);
            throw new RuntimeException("沙箱执行失败: " + response.getStatus() + ". Error: " + stderr);
        }

        // 6. 上传结果到 MinIO
        String zipName = "data_" + taskId + ".zip";
        File zipFile = new File(sandboxProperties.getShareDataPath(), zipName);
        if (!zipFile.exists()) {
            throw new RuntimeException("生成数据文件丢失: " + zipFile.getAbsolutePath());
        }

        log.info("开始上传结果至 MinIO: taskId={}", taskId);
        String bucketName = "results";
        String objectName = zipName;
        miniIOUtil.uploadFile(bucketName, zipFile.getAbsolutePath(), objectName);

        // 7. 清理本地文件
        FileUtil.del(zipFile);
        log.info("任务执行成功并已清理本地文件: taskId={}", taskId);

        com.maigen.common.core.model.dto.TaskResultDTO result = new com.maigen.common.core.model.dto.TaskResultDTO();
        result.setTaskId(taskId);
        result.setSuccess(true);
        result.setDownloadUrl(miniIOUtil.getPresignedUrl(bucketName, objectName));
        result.setBucketName(bucketName);
        result.setObjectName(objectName);
        return result;
    }
}
