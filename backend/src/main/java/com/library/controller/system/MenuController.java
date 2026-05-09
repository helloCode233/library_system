package com.library.controller.system;

import com.library.common.Result;
import com.library.entity.Menu;
import com.library.service.impl.MenuServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuServiceImpl menuService;

    @GetMapping("/tree")
    public Result<List<Menu>> tree() {
        return Result.success(menuService.tree());
    }

    @GetMapping("/nav")
    public Result<List<Menu>> nav(Authentication authentication) {
        Long userId = 1L;
        return Result.success(menuService.nav(userId));
    }

    @GetMapping("/{id}")
    public Result<Menu> getById(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Menu menu) {
        menuService.create(menu);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> update(@RequestBody Menu menu) {
        menuService.update(menu);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success("删除成功", null);
    }
}