package com.library.auth.controller;

import com.library.auth.common.Result;
import com.library.auth.dto.LoginRequest;
import com.library.auth.dto.LoginResponse;
import com.library.auth.entity.User;
import com.library.auth.service.AuthServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthServiceImpl authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return Result.success(loginResponse);
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        authService.register(user);
        return Result.success("注册成功", null);
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser(@RequestHeader("X-User-Id") String username) {
        User user = authService.getCurrentUser(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }
}
