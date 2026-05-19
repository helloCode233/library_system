package com.library.service.impl;

import com.library.entity.Book;
import com.library.entity.Borrow;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowServiceImpl {

    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private BookMapper bookMapper;

    @Transactional
    public void borrow(Long userId, Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getStock() <= 0) {
            throw new RuntimeException("库存不足");
        }

        book.setStock(book.getStock() - 1);
        bookMapper.updateById(book);

        Borrow borrow = new Borrow();
        borrow.setUserId(userId);
        borrow.setBookId(bookId);
        borrow.setBorrowDate(LocalDateTime.now());
        borrow.setDueDate(LocalDateTime.now().plusDays(30));
        borrow.setStatus(0);
        borrowMapper.insert(borrow);
    }

    @Transactional
    public void returnBook(Long borrowId) {
        Borrow borrow = borrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        if (borrow.getStatus() == 1) {
            throw new RuntimeException("该书已归还");
        }

        Book book = bookMapper.selectById(borrow.getBookId());
        if (book != null) {
            book.setStock(book.getStock() + 1);
            bookMapper.updateById(book);
        }

        borrow.setReturnDate(LocalDateTime.now());
        borrow.setStatus(1);
        borrowMapper.updateById(borrow);
    }

    public List<Borrow> getMyBorrows(Long userId) {
        return borrowMapper.selectByUserId(userId);
    }

    public List<Borrow> getAllBorrows() {
        return borrowMapper.selectAll();
    }
}