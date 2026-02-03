package com.maigen.sandbox.client;

import com.maigen.sandbox.config.SandboxProperties;
import com.maigen.sandbox.model.dto.GoJudgeRequest;
import com.maigen.sandbox.model.dto.GoJudgeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class GoJudgeClient {

    private final WebClient webClient;

    public GoJudgeClient(SandboxProperties properties) {
        log.info("初始化 GoJudgeClient, serverUrl: {}", properties.getServerUrl());
        this.webClient = WebClient.builder()
                .baseUrl(properties.getServerUrl())
                .build();
    }

    /**
     * 调用 go-judge 执行脚本
     */
    public List<GoJudgeResponse> execute(GoJudgeRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("发送 go-judge 请求: {}", cn.hutool.json.JSONUtil.toJsonStr(request));
        }
        
        return webClient.post()
                .uri("/run")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), GoJudgeRequest.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GoJudgeResponse>>() {})
                .block();
    }
}
