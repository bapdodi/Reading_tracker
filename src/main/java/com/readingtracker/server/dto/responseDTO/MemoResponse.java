package com.readingtracker.server.dto.responseDTO;

import java.time.LocalDateTime;
import java.util.List;

public class MemoResponse {
    private Long id;
    private Long userBookId;
    private String bookTitle;
    private String bookIsbn;
    private Integer pageNumber;  // 메모 작성 시점의 SESSION 모드 기준 초기 위치 (참조용 메타데이터)
    private String content;
    private List<String> tags;
    private LocalDateTime memoStartTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserBookId() { return userBookId; }
    public void setUserBookId(Long userBookId) { this.userBookId = userBookId; }
    
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    
    public String getBookIsbn() { return bookIsbn; }
    public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }
    
    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public LocalDateTime getMemoStartTime() { return memoStartTime; }
    public void setMemoStartTime(LocalDateTime memoStartTime) { this.memoStartTime = memoStartTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

