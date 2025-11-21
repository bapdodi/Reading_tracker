package com.readingtracker.server.dto.responseDTO;

import java.util.List;
import java.util.Map;

public class TagMemoGroup {
    private String tagCode;  // 태그 캐논컬 키 (프론트엔드에서 카탈로그를 참조하여 라벨 표시)
    private List<MemoResponse> memos;  // SESSION 모드: 타임라인 순으로 정렬됨
    private Map<Long, BookMemoGroup> memosByBook;  // TAG 모드: 태그 그룹 내부의 책별 하위 그룹
    private Integer memoCount;
    
    // Getters and Setters
    public String getTagCode() { return tagCode; }
    public void setTagCode(String tagCode) { this.tagCode = tagCode; }
    
    public List<MemoResponse> getMemos() { return memos; }
    public void setMemos(List<MemoResponse> memos) { this.memos = memos; }
    
    public Map<Long, BookMemoGroup> getMemosByBook() { return memosByBook; }
    public void setMemosByBook(Map<Long, BookMemoGroup> memosByBook) { this.memosByBook = memosByBook; }
    
    public Integer getMemoCount() { return memoCount; }
    public void setMemoCount(Integer memoCount) { this.memoCount = memoCount; }
}

