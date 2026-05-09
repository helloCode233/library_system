package com.library.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.entity.User;
import com.library.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/list")
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username) {
        return Result.success(userService.list(pageNum, pageSize, username));
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody User user,
                               @RequestParam(required = false) Long roleId) {
        userService.create(user, roleId);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> update(@RequestBody User user,
                               @RequestParam(required = false) Long roleId) {
        userService.update(user, roleId);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser(Authentication authentication) {
        User user = userService.getCurrentUser(authentication.getName());
        user.setPassword(null);
        return Result.success(user);
    }
}