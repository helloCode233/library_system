package com.library.controller;

import com.library.common.Result;
import com.library.service.impl.InsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/insight")
public class InsightController {

    @Autowired
    private InsightService insightService;

    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> hotBooks(
            @RequestParam(defaultValue = "10") int topN) {
        if (topN > 50) topN = 50;
        return Result.success(insightService.getHotBooks(topN));
    }

    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> monthlyTrend(
            @RequestParam(defaultValue = "12") int months) {
        if (months > 24) months = 24;
        return Result.success(insightService.getMonthlyTrend(months));
    }

    @GetMapping("/category")
    public Result<List<Map<String, Object>>> categoryDistribution() {
        return Result.success(insightService.getCategoryDistribution());
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(insightService.getOverview());
    }
}
