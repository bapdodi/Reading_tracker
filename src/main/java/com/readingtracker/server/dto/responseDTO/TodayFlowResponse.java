package com.readingtracker.server.dto.responseDTO;

import java.time.LocalDate;
import java.util.Map;

public class TodayFlowResponse {
    private LocalDate date;
    private Map<Long, BookMemoGroup> memosByBook;  // SESSION/BOOK 모드: 책별로 그룹화된 메모
    private Map<String, TagMemoGroup> memosByTag;  // TAG 모드: 태그별로 그룹화된 메모
    private Long totalMemoCount;
    private String sortBy;  // "SESSION" | "BOOK" | "TAG"
    
    // Getters and Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public Map<Long, BookMemoGroup> getMemosByBook() { return memosByBook; }
    public void setMemosByBook(Map<Long, BookMemoGroup> memosByBook) { this.memosByBook = memosByBook; }
    
    public Map<String, TagMemoGroup> getMemosByTag() { return memosByTag; }
    public void setMemosByTag(Map<String, TagMemoGroup> memosByTag) { this.memosByTag = memosByTag; }
    
    public Long getTotalMemoCount() { return totalMemoCount; }
    public void setTotalMemoCount(Long totalMemoCount) { this.totalMemoCount = totalMemoCount; }
    
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
}

