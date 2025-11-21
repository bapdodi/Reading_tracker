package com.readingtracker.dbms.entity;

import java.time.LocalDate;

/**
 * 알라딘 API에서 가져온 책 정보를 담는 비영속 Entity
 * DB에 저장되지 않으며, 외부 API 데이터를 내부 도메인 모델로 표현합니다.
 * 
 * 알라딘 API의 필드명(cover, pricesales, pubdate 등)을 
 * 우리 서비스의 표준 필드명(coverUrl, priceSales, publishedAt 등)으로 변환합니다.
 */
public class AladinBook {
    
    private String isbn;
    private String isbn13;
    private String title;
    private String author;
    private String publisher;
    private String description;
    private String coverUrl;  // 알라딘 API의 "cover" → "coverUrl"
    private Integer totalPages;
    private String mainGenre;
    private LocalDate publishedAt;  // 알라딘 API의 "pubdate" → "publishedAt"
    private Integer priceSales;  // 알라딘 API의 "pricesales" → "priceSales"
    private Integer priceStandard;  // 알라딘 API의 "pricestandard" → "priceStandard"
    
    // 기본 생성자
    public AladinBook() {}
    
    // 생성자
    public AladinBook(String isbn, String isbn13, String title, String author, String publisher) {
        this.isbn = isbn;
        this.isbn13 = isbn13;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
    }
    
    // 전체 생성자
    public AladinBook(String isbn, String isbn13, String title, String author, String publisher,
                      String description, String coverUrl, Integer totalPages, String mainGenre,
                      LocalDate publishedAt, Integer priceSales, Integer priceStandard) {
        this.isbn = isbn;
        this.isbn13 = isbn13;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.description = description;
        this.coverUrl = coverUrl;
        this.totalPages = totalPages;
        this.mainGenre = mainGenre;
        this.publishedAt = publishedAt;
        this.priceSales = priceSales;
        this.priceStandard = priceStandard;
    }
    
    // Getters
    public String getIsbn() { return isbn; }
    public String getIsbn13() { return isbn13; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPublisher() { return publisher; }
    public String getDescription() { return description; }
    public String getCoverUrl() { return coverUrl; }
    public Integer getTotalPages() { return totalPages; }
    public String getMainGenre() { return mainGenre; }
    public LocalDate getPublishedAt() { return publishedAt; }
    public Integer getPriceSales() { return priceSales; }
    public Integer getPriceStandard() { return priceStandard; }
    
    // Setters
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setIsbn13(String isbn13) { this.isbn13 = isbn13; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public void setDescription(String description) { this.description = description; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    public void setMainGenre(String mainGenre) { this.mainGenre = mainGenre; }
    public void setPublishedAt(LocalDate publishedAt) { this.publishedAt = publishedAt; }
    public void setPriceSales(Integer priceSales) { this.priceSales = priceSales; }
    public void setPriceStandard(Integer priceStandard) { this.priceStandard = priceStandard; }
}

