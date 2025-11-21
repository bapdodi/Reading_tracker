package com.readingtracker.server.dto.responseDTO;

import java.util.List;
import java.util.Map;

/**
 * 알라딘 API 원본 응답 구조를 담는 외부 DTO
 * 알라딘 API의 JSON 응답 필드명을 그대로 사용합니다.
 */
public class AladinBookResponseDTO {
    
    private Integer totalResults;
    private Integer startIndex;
    private Integer itemsPerPage;
    private List<AladinBookItemDTO> items;
    
    // 기본 생성자
    public AladinBookResponseDTO() {}
    
    // 생성자
    public AladinBookResponseDTO(Integer totalResults, Integer startIndex, Integer itemsPerPage, List<AladinBookItemDTO> items) {
        this.totalResults = totalResults;
        this.startIndex = startIndex;
        this.itemsPerPage = itemsPerPage;
        this.items = items;
    }
    
    // Getters
    public Integer getTotalResults() { return totalResults; }
    public Integer getStartIndex() { return startIndex; }
    public Integer getItemsPerPage() { return itemsPerPage; }
    public List<AladinBookItemDTO> getItems() { return items; }
    
    // Setters
    public void setTotalResults(Integer totalResults) { this.totalResults = totalResults; }
    public void setStartIndex(Integer startIndex) { this.startIndex = startIndex; }
    public void setItemsPerPage(Integer itemsPerPage) { this.itemsPerPage = itemsPerPage; }
    public void setItems(List<AladinBookItemDTO> items) { this.items = items; }
    
    /**
     * 알라딘 API의 개별 책 정보 DTO
     * 알라딘 API의 필드명을 그대로 사용합니다.
     */
    public static class AladinBookItemDTO {
        private String isbn;
        private String isbn13;
        private String title;
        private String author;
        private String publisher;
        private String description;
        private String cover;  // 알라딘 API 필드명: cover
        private Integer pricesales;  // 알라딘 API 필드명: pricesales
        private Integer pricestandard;  // 알라딘 API 필드명: pricestandard
        private String pubdate;  // 알라딘 API 필드명: pubdate (yyyy-MM-dd 형식 문자열)
        private Map<String, Object> subInfo;  // 알라딘 API의 subInfo 객체 (페이지 수, 장르 등 포함)
        
        // 기본 생성자
        public AladinBookItemDTO() {}
        
        // Getters
        public String getIsbn() { return isbn; }
        public String getIsbn13() { return isbn13; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getPublisher() { return publisher; }
        public String getDescription() { return description; }
        public String getCover() { return cover; }
        public Integer getPricesales() { return pricesales; }
        public Integer getPricestandard() { return pricestandard; }
        public String getPubdate() { return pubdate; }
        public Map<String, Object> getSubInfo() { return subInfo; }
        
        // Setters
        public void setIsbn(String isbn) { this.isbn = isbn; }
        public void setIsbn13(String isbn13) { this.isbn13 = isbn13; }
        public void setTitle(String title) { this.title = title; }
        public void setAuthor(String author) { this.author = author; }
        public void setPublisher(String publisher) { this.publisher = publisher; }
        public void setDescription(String description) { this.description = description; }
        public void setCover(String cover) { this.cover = cover; }
        public void setPricesales(Integer pricesales) { this.pricesales = pricesales; }
        public void setPricestandard(Integer pricestandard) { this.pricestandard = pricestandard; }
        public void setPubdate(String pubdate) { this.pubdate = pubdate; }
        public void setSubInfo(Map<String, Object> subInfo) { this.subInfo = subInfo; }
    }
}

