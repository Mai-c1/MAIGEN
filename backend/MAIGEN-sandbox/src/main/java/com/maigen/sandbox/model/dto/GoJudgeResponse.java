package com.maigen.sandbox.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * go-judge 响应报文
 */
@Data
public class GoJudgeResponse {
    private String status;
    private Integer exitStatus;
    private Long time;
    private Long memory;
    private Map<String, String> files;
}
