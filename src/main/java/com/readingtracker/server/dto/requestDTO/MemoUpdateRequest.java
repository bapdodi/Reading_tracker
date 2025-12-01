package com.readingtracker.server.dto.requestDTO;

import jakarta.validation.constraints.*;
import java.util.List;

public class MemoUpdateRequest {
    
    @Size(max = 5000, message = "메모 내용은 5000자를 초과할 수 없습니다.")
    private String content;
    
    private List<String> tags;
    
    private String tagCategory;  // 태그 대분류 (TYPE, TOPIC) - 태그 미선택 시 etc 태그 선택에 사용
    
    // 참고: pageNumber는 메모 작성 시점의 시작 위치를 나타내는 메타데이터이므로 수정 불가
    // 메모 수정 시에도 원본 위치 정보를 보존해야 하며, UI 레이아웃은 프론트엔드에서 처리합니다.
    
    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public String getTagCategory() { return tagCategory; }
    public void setTagCategory(String tagCategory) { this.tagCategory = tagCategory; }
}

