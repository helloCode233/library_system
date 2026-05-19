package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_borrow")
public class Borrow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private Integer status; // 0=借阅中, 1=已归还, 2=已逾期
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // transient fields for display
    private String bookTitle;
    private String bookIsbn;
    private String username;
}