package com.library.service.impl;

import com.library.common.TfidfUtil;
import com.library.entity.Book;
import com.library.entity.Borrow;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BorrowMapper borrowMapper;

    private Map<String, Map<String, Double>> bookVectors = new LinkedHashMap<>();
    private Map<String, Book> bookCache = new LinkedHashMap<>();
    private volatile boolean initialized = false;

    @PostConstruct
    public void init() {
        buildIndex();
    }

    public synchronized void buildIndex() {
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

    public List<Map<String, Object>> recommendByBook(Long bookId, int topN) {
        if (!initialized) buildIndex();
        String sourceId = bookId.toString();
        Map<String, Double> sourceVec = bookVectors.get(sourceId);
        if (sourceVec == null || sourceVec.isEmpty()) {
            return getHotBooks(topN);
        }
        List<Map.Entry<String, Double>> scored = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : bookVectors.entrySet()) {
            if (entry.getKey().equals(sourceId)) continue;
            double sim = TfidfUtil.cosineSimilarity(sourceVec, entry.getValue());
            if (sim > 0) {
                scored.add(new AbstractMap.SimpleEntry<>(entry.getKey(), sim));
            }
        }
        scored.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return toResult(scored, topN);
    }

    public List<Map<String, Object>> recommendByHistory(Long userId, int topN) {
        if (!initialized) buildIndex();
        List<Borrow> borrows = borrowMapper.selectByUserId(userId);
        Set<String> borrowedBookIds = borrows.stream()
                .map(b -> b.getBookId().toString())
                .collect(Collectors.toSet());
        if (borrowedBookIds.isEmpty()) {
            return getHotBooks(topN);
        }
        List<Map<String, Double>> borrowedVecs = new ArrayList<>();
        for (String bid : borrowedBookIds) {
            Map<String, Double> vec = bookVectors.get(bid);
            if (vec != null && !vec.isEmpty()) {
                borrowedVecs.add(vec);
            }
        }
        if (borrowedVecs.isEmpty()) {
            return getHotBooks(topN);
        }
        List<Map.Entry<String, Double>> scored = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : bookVectors.entrySet()) {
            if (borrowedBookIds.contains(entry.getKey())) continue;
            double totalSim = 0.0;
            for (Map<String, Double> bv : borrowedVecs) {
                totalSim += TfidfUtil.cosineSimilarity(entry.getValue(), bv);
            }
            double avgSim = totalSim / borrowedVecs.size();
            if (avgSim > 0) {
                scored.add(new AbstractMap.SimpleEntry<>(entry.getKey(), avgSim));
            }
        }
        scored.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        List<Map<String, Object>> results = toResult(scored, topN);
        if (results.size() < topN) {
            Set<String> existingIds = results.stream()
                    .map(r -> r.get("id").toString()).collect(Collectors.toSet());
            List<Map<String, Object>> hotBooks = getHotBooks(topN);
            for (Map<String, Object> hot : hotBooks) {
                if (results.size() >= topN) break;
                if (!existingIds.contains(hot.get("id").toString()) && !borrowedBookIds.contains(hot.get("id").toString())) {
                    results.add(hot);
                }
            }
        }
        return results;
    }

    public List<Map<String, Object>> getHotBooks(int topN) {
        List<Borrow> allBorrows = borrowMapper.selectAll();
        Map<Long, Long> borrowCounts = allBorrows.stream()
                .collect(Collectors.groupingBy(Borrow::getBookId, Collectors.counting()));
        List<Map.Entry<Long, Long>> sorted = borrowCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .collect(Collectors.toList());
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : sorted) {
            if (results.size() >= topN) break;
            Book book = bookCache.get(entry.getKey().toString());
            if (book == null) {
                book = bookMapper.selectById(entry.getKey());
                if (book != null) bookCache.put(entry.getKey().toString(), book);
            }
            if (book != null) {
                Map<String, Object> item = bookToMap(book);
                item.put("borrowCount", entry.getValue());
                item.put("score", (double) entry.getValue());
                results.add(item);
            }
        }
        return results;
    }

    private List<Map<String, Object>> toResult(List<Map.Entry<String, Double>> scored, int topN) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map.Entry<String, Double> entry : scored) {
            if (results.size() >= topN) break;
            Book book = bookCache.get(entry.getKey());
            if (book != null) {
                Map<String, Object> item = bookToMap(book);
                item.put("score", Math.round(entry.getValue() * 10000.0) / 10000.0);
                results.add(item);
            }
        }
        return results;
    }

    private Map<String, Object> bookToMap(Book book) {
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
        item.put("categoryName", book.getCategoryName());
        return item;
    }
}
