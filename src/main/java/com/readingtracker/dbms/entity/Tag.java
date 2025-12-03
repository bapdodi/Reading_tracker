package com.readingtracker.dbms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.sharedsync.shared.annotation.CacheEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@CacheEntity
@Builder
@AllArgsConstructor
@Entity
@Table(name = "tags")
@EntityListeners(AuditingEntityListener.class)
public class Tag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private TagCategory category;  // TYPE 또는 TOPIC
    
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;  // 캐논컬 키 (예: 'impressive-quote')
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;  // 정렬 순서 (가나다순)
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;  // 활성화 상태
    
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private List<Memo> memos = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Tag() {}
    
    public Tag(TagCategory category, String code, Integer sortOrder) {
        this.category = category;
        this.code = code;
        this.sortOrder = sortOrder;
        this.isActive = true;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public TagCategory getCategory() { return category; }
    public void setCategory(TagCategory category) { this.category = category; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public List<Memo> getMemos() { return memos; }
    public void setMemos(List<Memo> memos) { this.memos = memos; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

