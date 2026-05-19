package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Book;
import com.library.mapper.BookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl {

    @Autowired
    private BookMapper bookMapper;

    public Page<Book> list(Integer pageNum, Integer pageSize, String title, String author, Long categoryId) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(Book::getTitle, title);
        }
        if (author != null && !author.isEmpty()) {
            wrapper.like(Book::getAuthor, author);
        }
        if (categoryId != null) {
            wrapper.eq(Book::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Book::getCreateTime);
        return bookMapper.selectPage(page, wrapper);
    }

    public Book getById(Long id) {
        return bookMapper.selectById(id);
    }

    public void create(Book book) {
        // Check ISBN uniqueness
        Book exist = bookMapper.selectOne(
                new LambdaQueryWrapper<Book>().eq(Book::getIsbn, book.getIsbn())
        );
        if (exist != null) {
            throw new RuntimeException("ISBN 已存在");
        }
        if (book.getTotal() == null) {
            book.setTotal(book.getStock());
        }
        bookMapper.insert(book);
    }

    public void update(Book book) {
        bookMapper.updateById(book);
    }

    public void delete(Long id) {
        bookMapper.deleteById(id);
    }

    public void updateStock(Long id, int delta) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        int newStock = book.getStock() + delta;
        if (newStock < 0) {
            throw new RuntimeException("库存不足");
        }
        book.setStock(newStock);
        bookMapper.updateById(book);
    }
}
