package com.maigen.api.service;

import com.maigen.api.entity.CommunityContent;
import com.baomidou.mybatisplus.extension.service.IService;

import com.maigen.api.model.dto.CommunityQuery;
import com.maigen.api.model.dto.CommunityRatingDTO;
import com.maigen.api.model.dto.CommunityShareDTO;
import com.maigen.api.model.dto.PageDTO;
import com.maigen.api.model.vo.CommunityDetailVO;
import com.maigen.api.model.vo.CommunityVO;

/**
* @author 25128
* @description 针对表【community_content】的数据库操作Service
* @createDate 2026-01-29 19:57:59
*/
public interface CommunityContentService extends IService<CommunityContent> {

    /**
     * 分享内容
     */
    Long share(CommunityShareDTO dto);

    /**
     * 分页查询社区内容
     */
    PageDTO<CommunityVO> getPage(CommunityQuery query);

    /**
     * 获取详情
     */
    CommunityDetailVO getDetail(Long id);

    /**
     * 下载资源 (处理积分逻辑)
     */
    String download(Long contentId);

    /**
     * 点赞/取消点赞
     */
    void like(Long id);

    /**
     * 评分
     */
    void rate(CommunityRatingDTO dto);
}
