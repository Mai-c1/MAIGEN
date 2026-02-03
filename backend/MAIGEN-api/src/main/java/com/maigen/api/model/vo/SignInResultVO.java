package com.maigen.api.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "签到结果")
public class SignInResultVO {

    @Schema(description = "本次获得总积分")
    private Integer rewardPoints;

    @Schema(description = "当前总积分余额")
    private Integer totalPoints;
}
