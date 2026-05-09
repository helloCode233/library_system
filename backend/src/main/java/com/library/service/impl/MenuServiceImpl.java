package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.Menu;
import com.library.mapper.MenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl {

    @Autowired
    private MenuMapper menuMapper;

    public List<Menu> tree() {
        List<Menu> all = menuMapper.selectList(null);
        return buildTree(all, 0L);
    }

    public List<Menu> nav(Long userId) {
        return menuMapper.selectNavByUserId(userId);
    }

    public Menu getById(Long id) {
        return menuMapper.selectById(id);
    }

    public void create(Menu menu) {
        menuMapper.insert(menu);
    }

    public void update(Menu menu) {
        menuMapper.updateById(menu);
    }

    public void delete(Long id) {
        menuMapper.deleteById(id);
    }

    private List<Menu> buildTree(List<Menu> all, Long parentId) {
        return all.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .peek(m -> m.setChildren(buildTree(all, m.getId())))
                .collect(Collectors.toList());
    }
}