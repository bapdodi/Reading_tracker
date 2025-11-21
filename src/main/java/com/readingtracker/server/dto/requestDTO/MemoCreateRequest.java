package com.readingtracker.server.dto.requestDTO;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public class MemoCreateRequest {
    
    @NotNull(message = "책 ID는 필수입니다.")
    private Long userBookId;  // UserShelfBook의 id
    
    @NotNull(message = "페이지 수는 필수입니다.")
    @Min(value = 1, message = "페이지 수는 1 이상이어야 합니다.")
    private Integer pageNumber;  // 메모 작성 시점의 SESSION 모드 기준 초기 위치
    
    @NotBlank(message = "메모 내용은 필수입니다.")
    @Size(max = 5000, message = "메모 내용은 5000자를 초과할 수 없습니다.")
    private String content;
    
    private List<String> tags;  // 메모 분류 태그 리스트 (하나 이상의 태그 설정 가능)
    
    @NotNull(message = "메모 시작 시간은 필수입니다.")
    private LocalDateTime memoStartTime;
    
    // Getters and Setters
    public Long getUserBookId() { return userBookId; }
    public void setUserBookId(Long userBookId) { this.userBookId = userBookId; }
    
    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public LocalDateTime getMemoStartTime() { return memoStartTime; }
    public void setMemoStartTime(LocalDateTime memoStartTime) { this.memoStartTime = memoStartTime; }
}

