package com.maigen.sandbox.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * go-judge 请求报文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoJudgeRequest {
    private List<Cmd> cmd;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Cmd {
        private List<String> args;
        private List<String> env;
        private List<Map<String, Object>> files;
        private Long cpuLimit;
        private Long memoryLimit;
        private Long clockLimit;
        private Integer procLimit;
        private Map<String, FileContent> copyIn;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FileContent {
        private String content;
    }
}
