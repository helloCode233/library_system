package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Category;
import com.library.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl {

    @Autowired
    private CategoryMapper categoryMapper;

    public Page<Category> list(Integer pageNum, Integer pageSize) {
        Page<Category> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Category::getSort);
        return categoryMapper.selectPage(page, wrapper);
    }

    public java.util.List<Category> listAll() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        return categoryMapper.selectList(wrapper);
    }

    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }

    public void create(Category category) {
        Category exist = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>().eq(Category::getName, category.getName())
        );
        if (exist != null) {
            throw new RuntimeException("分类名称已存在");
        }
        categoryMapper.insert(category);
    }

    public void update(Category category) {
        categoryMapper.updateById(category);
    }

    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
