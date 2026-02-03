package com.maigen.api.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@Schema(description = "分页参数")
public class PageDTO<T> {

    @Schema(description = "总条数")
    Long total;
    @Schema(description = "总页数")
    Long pages;
    @Schema(description = "结果集")
    List<T> records;

    public static <PO, VO> PageDTO<VO> of(Page<PO> page, Function<PO, VO> convertor) {
        //将MP的分页po结果转换成vo对象
        PageDTO<VO> pageDTO = new PageDTO<>();
        pageDTO.setPages(page.getPages());
        pageDTO.setTotal(page.getTotal());
        List<PO> list = page.getRecords();
        pageDTO.setRecords(list.stream().map(convertor).filter(Objects::nonNull).collect(Collectors.toList()));
        return pageDTO;
    }

    public static <T> PageDTO<T> empty() {
        PageDTO<T> result = new PageDTO<>();
        result.setTotal(0L);
        result.setPages(0L);
        result.setRecords(new ArrayList<>());
        return result;
    }

}
