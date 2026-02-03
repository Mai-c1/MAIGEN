package com.maigen.api.model.dto;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "分页查询实体")
public class PageQuery implements Serializable {

    @Schema(description = "页码")
    protected Integer page = 1;
    @Schema(description = "页大小")
    protected Integer pageSize = 10;
    @Schema(description = "排序字段")
    protected String sortBy;
    @Schema(description = "是否升序排序")
    protected Boolean isAsc;

    public <T> Page<T> toMpPage(OrderItem... items) {
        Page<T> ipage = Page.of(page, pageSize);

        if (StrUtil.isNotBlank(sortBy)) {
            ipage.addOrder(isAsc ? OrderItem.asc(sortBy) : OrderItem.desc(sortBy));
        } else {
            ipage.addOrder(items);
        }
        return ipage;
    }

}
