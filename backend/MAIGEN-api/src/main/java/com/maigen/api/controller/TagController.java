package com.maigen.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.maigen.api.model.vo.TagVO;
import com.maigen.api.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
@Tag(name = "标签管理", description = "内容标签查询")
@SaCheckLogin
public class TagController {

    private final TagService tagService;

    @GetMapping("/list")
    @Operation(summary = "标签列表")
    public SaResult list() {
        List<TagVO> list = tagService.list().stream()
                .map(t -> TagVO.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .build())
                .collect(Collectors.toList());
        return SaResult.data(list);
    }
}
