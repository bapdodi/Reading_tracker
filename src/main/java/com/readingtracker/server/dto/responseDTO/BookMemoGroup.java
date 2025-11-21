package com.readingtracker.server.dto.responseDTO;

import java.util.List;
import java.util.Map;

public class BookMemoGroup {
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private List<MemoResponse> memos;  // BOOK 모드: 책 그룹 내부의 메모(시간순)
    private Map<String, TagMemoGroup> memosByTag;  // TAG 모드: 책 그룹 내부의 태그별 하위 그룹
    private Integer memoCount;
    private String sortBy;  // "SESSION" | "BOOK" | "TAG"
    
    // Getters and Setters
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    
    public String getBookIsbn() { return bookIsbn; }
    public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }
    
    public List<MemoResponse> getMemos() { return memos; }
    public void setMemos(List<MemoResponse> memos) { this.memos = memos; }
    
    public Map<String, TagMemoGroup> getMemosByTag() { return memosByTag; }
    public void setMemosByTag(Map<String, TagMemoGroup> memosByTag) { this.memosByTag = memosByTag; }
    
    public Integer getMemoCount() { return memoCount; }
    public void setMemoCount(Integer memoCount) { this.memoCount = memoCount; }
    
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
}

