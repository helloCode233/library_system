package com.library.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
