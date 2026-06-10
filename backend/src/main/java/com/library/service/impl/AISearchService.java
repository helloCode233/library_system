package com.library.service.impl;

import com.library.common.TfidfUtil;
import com.library.entity.Book;
import com.library.mapper.BookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AISearchService {

    @Autowired
    private BookMapper bookMapper;

    private Map<String, Map<String, Double>> bookVectors = new LinkedHashMap<>();
    private Map<String, Book> bookCache = new LinkedHashMap<>();
    private volatile boolean initialized = false;

    @PostConstruct
    public void init() {
        rebuildIndex();
    }

    public synchronized void rebuildIndex() {
        List<Book> books = bookMapper.selectList(null);
        Map<String, List<String>> docs = new LinkedHashMap<>();
        bookCache.clear();
        for (Book book : books) {
            String docId = book.getId().toString();
            String text = String.join(" ",
                    book.getTitle() != null ? book.getTitle() : "",
                    book.getAuthor() != null ? book.getAuthor() : "",
                    book.getDescription() != null ? book.getDescription() : ""
            );
            docs.put(docId, TfidfUtil.tokenize(text));
            bookCache.put(docId, book);
        }
        this.bookVectors = TfidfUtil.computeTfidfVectors(docs);
        this.initialized = true;
    }

    public List<Map<String, Object>> search(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        if (!initialized) {
            rebuildIndex();
        }
        List<Map.Entry<String, Double>> ranked = TfidfUtil.search(query.trim(), bookVectors);
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map.Entry<String, Double> entry : ranked) {
            if (results.size() >= limit) break;
            Book book = bookCache.get(entry.getKey());
            if (book != null) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", book.getId());
                item.put("title", book.getTitle());
                item.put("author", book.getAuthor());
                item.put("isbn", book.getIsbn());
                item.put("categoryId", book.getCategoryId());
                item.put("description", book.getDescription());
                item.put("coverUrl", book.getCoverUrl());
                item.put("stock", book.getStock());
                item.put("status", book.getStatus());
                item.put("score", Math.round(entry.getValue() * 10000.0) / 10000.0);
                results.add(item);
            }
        }
        return results;
    }

    public int getIndexedCount() {
        return bookCache.size();
    }
}
