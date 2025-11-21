package com.readingtracker.server.dto.requestDTO;

import jakarta.validation.constraints.*;

public class CloseBookRequest {
    
    @NotNull(message = "마지막으로 읽은 페이지 수는 필수입니다.")
    @Min(value = 1, message = "페이지 수는 1 이상이어야 합니다.")
    private Integer lastReadPage;
    
    // Getters and Setters
    public Integer getLastReadPage() { return lastReadPage; }
    public void setLastReadPage(Integer lastReadPage) { this.lastReadPage = lastReadPage; }
}

