package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    @Select("SELECT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id = #{roleId} AND m.status = 1 " +
            "ORDER BY m.sort")
    List<Menu> selectByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT m.* FROM sys_menu m " +
            "INNER JOIN sys_user_role ur ON 1=1 " +
            "INNER JOIN sys_role_menu rm ON ur.role_id = rm.role_id AND m.id = rm.menu_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 " +
            "ORDER BY m.sort")
    List<Menu> selectNavByUserId(@Param("userId") Long userId);
}