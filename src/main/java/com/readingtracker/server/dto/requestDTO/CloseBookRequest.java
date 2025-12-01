package com.readingtracker.server.dto.requestDTO;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class CloseBookRequest {
    
    @NotNull(message = "마지막으로 읽은 페이지 수는 필수입니다.")
    @Min(value = 1, message = "페이지 수는 1 이상이어야 합니다.")
    private Integer lastReadPage;
    
    // Finished 카테고리로 변경될 경우 필요한 필드
    private LocalDate readingFinishedDate;  // 독서 종료일 (필수, Finished일 때만)
    
    @Min(value = 1, message = "평점은 1 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5 이하여야 합니다.")
    private Integer rating;  // 평점 (필수, Finished일 때만)
    
    private String review;  // 후기 (선택, Finished일 때만)
    
    // Getters and Setters
    public Integer getLastReadPage() { return lastReadPage; }
    public void setLastReadPage(Integer lastReadPage) { this.lastReadPage = lastReadPage; }
    
    public LocalDate getReadingFinishedDate() { return readingFinishedDate; }
    public void setReadingFinishedDate(LocalDate readingFinishedDate) { this.readingFinishedDate = readingFinishedDate; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
}

