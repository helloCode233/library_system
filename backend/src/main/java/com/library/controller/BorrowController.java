package com.library.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.Result;
import com.library.entity.Borrow;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.service.impl.BorrowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    @Autowired
    private BorrowServiceImpl borrowService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping
    public Result<Void> borrow(@RequestBody Map<String, Long> params,
                               @RequestHeader("X-User-Id") String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        borrowService.borrow(user.getId(), params.get("bookId"));
        return Result.success("借书成功", null);
    }

    @PutMapping("/{id}/return")
    public Result<Void> returnBook(@PathVariable Long id) {
        borrowService.returnBook(id);
        return Result.success("还书成功", null);
    }

    @GetMapping("/my")
    public Result<List<Borrow>> myBorrows(@RequestHeader("X-User-Id") String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(borrowService.getMyBorrows(user.getId()));
    }

    @GetMapping("/list")
    public Result<List<Borrow>> list() {
        return Result.success(borrowService.getAllBorrows());
    }
}