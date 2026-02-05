package com.maigen.api.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maigen.api.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maigen.api.model.dto.admin.AdminUserQueryDTO;
import com.maigen.api.model.vo.AdminUserVO;
import org.apache.ibatis.annotations.Param;

/**
* @author 25128
* @description 针对表【user】的数据库操作Mapper
* @createDate 2026-01-30 00:10:49
* @Entity com.maigen.api.entity.User
*/
public interface UserMapper extends BaseMapper<User> {

}




