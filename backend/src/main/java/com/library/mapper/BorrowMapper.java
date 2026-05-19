package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Borrow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BorrowMapper extends BaseMapper<Borrow> {

    @Select("SELECT b.*, u.username, book.title as book_title, book.isbn as book_isbn " +
            "FROM sys_borrow b " +
            "INNER JOIN sys_user u ON b.user_id = u.id " +
            "INNER JOIN sys_book book ON b.book_id = book.id " +
            "WHERE b.user_id = #{userId} " +
            "ORDER BY b.create_time DESC")
    List<Borrow> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT b.*, u.username, book.title as book_title, book.isbn as book_isbn " +
            "FROM sys_borrow b " +
            "INNER JOIN sys_user u ON b.user_id = u.id " +
            "INNER JOIN sys_book book ON b.book_id = book.id " +
            "ORDER BY b.create_time DESC")
    List<Borrow> selectAll();
}