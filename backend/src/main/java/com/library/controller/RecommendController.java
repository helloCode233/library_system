package com.library.controller;

import com.library.common.Result;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.service.impl.RecommendService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/recommend/book/{id}")
    public Result<List<Map<String, Object>>> recommendByBook(
            @PathVariable Long id,
            @RequestParam(defaultValue = "6") int topN) {
        if (topN > 20) topN = 20;
        return Result.success(recommendService.recommendByBook(id, topN));
    }

    @GetMapping("/recommend/user")
    public Result<List<Map<String, Object>>> recommendByHistory(
            @RequestHeader("X-User-Id") String username,
            @RequestParam(defaultValue = "6") int topN) {
        if (topN > 20) topN = 20;
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(recommendService.recommendByHistory(user.getId(), topN));
    }

    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> hotBooks(
            @RequestParam(defaultValue = "10") int topN) {
        if (topN > 50) topN = 50;
        return Result.success(recommendService.getHotBooks(topN));
    }
}
