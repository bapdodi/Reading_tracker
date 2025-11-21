package com.readingtracker.server.dto.responseDTO;

import com.readingtracker.server.common.constant.BookCategory;
import java.time.LocalDateTime;

public class BookResponse {
    private Long id;  // UserShelfBook.id
    private String title;
    private String author;
    private String isbn;
    private BookCategory category;
    private Integer readingProgress;
    private LocalDateTime lastMemoTime;  // 최신 메모 작성 시간 (선택)
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public BookCategory getCategory() { return category; }
    public void setCategory(BookCategory category) { this.category = category; }
    
    public Integer getReadingProgress() { return readingProgress; }
    public void setReadingProgress(Integer readingProgress) { this.readingProgress = readingProgress; }
    
    public LocalDateTime getLastMemoTime() { return lastMemoTime; }
    public void setLastMemoTime(LocalDateTime lastMemoTime) { this.lastMemoTime = lastMemoTime; }
}

