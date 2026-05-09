package com.library.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.entity.Category;
import com.library.service.impl.CategoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryServiceImpl categoryService;

    @GetMapping("/list")
    public Result<Page<Category>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(categoryService.list(pageNum, pageSize));
    }

    @GetMapping("/all")
    public Result<List<Category>> listAll() {
        return Result.success(categoryService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Category category) {
        categoryService.create(category);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> update(@RequestBody Category category) {
        categoryService.update(category);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success("删除成功", null);
    }
}
