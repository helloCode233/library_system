package com.library.service.impl;

import com.library.entity.Book;
import com.library.entity.Borrow;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InsightService {

    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private BookMapper bookMapper;

    public List<Map<String, Object>> getHotBooks(int topN) {
        List<Borrow> allBorrows = borrowMapper.selectAll();
        Map<Long, Long> borrowCounts = allBorrows.stream()
                .collect(Collectors.groupingBy(Borrow::getBookId, Collectors.counting()));
        List<Map.Entry<Long, Long>> sorted = borrowCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .collect(Collectors.toList());
        List<Map<String, Object>> results = new ArrayList<>();
        int rank = 0;
        for (Map.Entry<Long, Long> entry : sorted) {
            if (results.size() >= topN) break;
            rank++;
            Book book = bookMapper.selectById(entry.getKey());
            if (book != null) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("rank", rank);
                item.put("bookId", book.getId());
                item.put("title", book.getTitle());
                item.put("author", book.getAuthor());
                item.put("categoryId", book.getCategoryId());
                item.put("stock", book.getStock());
                item.put("borrowCount", entry.getValue());
                results.add(item);
            }
        }
        return results;
    }

    public List<Map<String, Object>> getMonthlyTrend(int months) {
        List<Borrow> allBorrows = borrowMapper.selectAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Long> monthlyCounts = allBorrows.stream()
                .filter(b -> b.getBorrowDate() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getBorrowDate().format(fmt),
                        Collectors.counting()
                ));
        List<Map.Entry<String, Long>> sorted = monthlyCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());
        int start = Math.max(0, sorted.size() - months);
        List<Map<String, Object>> results = new ArrayList<>();
        for (int i = start; i < sorted.size(); i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", sorted.get(i).getKey());
            item.put("count", sorted.get(i).getValue());
            results.add(item);
        }
        return results;
    }

    public List<Map<String, Object>> getCategoryDistribution() {
        List<Borrow> allBorrows = borrowMapper.selectAll();
        List<Book> allBooks = bookMapper.selectList(null);
        Map<Long, Long> bookCategoryMap = allBooks.stream()
                .collect(Collectors.toMap(Book::getId, b -> b.getCategoryId() != null ? b.getCategoryId() : 0L));
        Map<Long, Long> categoryCounts = new HashMap<>();
        for (Borrow borrow : allBorrows) {
            Long catId = bookCategoryMap.getOrDefault(borrow.getBookId(), 0L);
            categoryCounts.merge(catId, 1L, Long::sum);
        }
        long total = categoryCounts.values().stream().mapToLong(Long::longValue).sum();
        List<Map<String, Object>> results = new ArrayList<>();
        Map<Long, String> categoryNames = new HashMap<>();
        for (Book book : allBooks) {
            if (book.getCategoryId() != null && book.getCategoryName() != null) {
                categoryNames.putIfAbsent(book.getCategoryId(), book.getCategoryName());
            }
        }
        for (Map.Entry<Long, Long> entry : categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .collect(Collectors.toList())) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("categoryId", entry.getKey());
            item.put("categoryName", categoryNames.getOrDefault(entry.getKey(), "未分类"));
            item.put("borrowCount", entry.getValue());
            item.put("percentage", total > 0 ? Math.round(entry.getValue() * 10000.0 / total) / 100.0 : 0.0);
            results.add(item);
        }
        return results;
    }

    public Map<String, Object> getOverview() {
        List<Borrow> allBorrows = borrowMapper.selectAll();
        long totalBorrows = allBorrows.size();
        long activeBorrows = allBorrows.stream().filter(b -> b.getStatus() == 0).count();
        long totalReturns = allBorrows.stream().filter(b -> b.getStatus() == 1).count();
        long totalUsers = allBorrows.stream().map(Borrow::getUserId).distinct().count();
        long totalBooksInvolved = allBorrows.stream().map(Borrow::getBookId).distinct().count();
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalBorrows", totalBorrows);
        overview.put("activeBorrows", activeBorrows);
        overview.put("totalReturns", totalReturns);
        overview.put("totalUsers", totalUsers);
        overview.put("totalBooksInvolved", totalBooksInvolved);
        return overview;
    }
}
