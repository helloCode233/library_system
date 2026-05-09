package com.library.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.entity.Menu;
import com.library.entity.Role;
import com.library.service.impl.RoleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleServiceImpl roleService;

    @GetMapping("/list")
    public Result<Page<Role>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(roleService.list(pageNum, pageSize));
    }

    @GetMapping("/all")
    public Result<List<Role>> listAll() {
        return Result.success(roleService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @GetMapping("/{id}/menus")
    public Result<List<Menu>> getMenus(@PathVariable Long id) {
        return Result.success(roleService.getMenusByRoleId(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Role role) {
        roleService.create(role);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> update(@RequestBody Role role) {
        roleService.update(role);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success("删除成功", null);
    }

    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.success("分配成功", null);
    }
}