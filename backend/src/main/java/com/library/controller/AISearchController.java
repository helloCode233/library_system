package com.library.controller;

import com.library.common.Result;
import com.library.service.impl.AISearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AISearchController {

    @Autowired
    private AISearchService aiSearchService;

    @PostMapping("/search")
    public Result<List<Map<String, Object>>> search(@RequestBody Map<String, Object> params) {
        String query = (String) params.getOrDefault("query", "");
        int limit = params.containsKey("limit") ? ((Number) params.get("limit")).intValue() : 10;
        if (limit > 50) limit = 50;
        List<Map<String, Object>> results = aiSearchService.search(query, limit);
        return Result.success(results);
    }

    @PostMapping("/rebuild-index")
    public Result<Map<String, Object>> rebuildIndex() {
        aiSearchService.rebuildIndex();
        Map<String, Object> info = new java.util.LinkedHashMap<>();
        info.put("indexedCount", aiSearchService.getIndexedCount());
        info.put("status", "ok");
        return Result.success("索引重建成功", info);
    }
}
