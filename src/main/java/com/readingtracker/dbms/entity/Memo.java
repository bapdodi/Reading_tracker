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
@Table(name = "memo")
@EntityListeners(AuditingEntityListener.class)
public class Memo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private UserShelfBook userShelfBook;  // User_Books 테이블 참조
    
    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;  // 메모 작성 시점의 SESSION 모드 기준 초기 위치 (정렬 방식 변경 시에도 변경하지 않는 메타데이터)
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "memo_tags",
        joinColumns = @JoinColumn(name = "memo_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();
    
    /**
     * 메모가 작성된 시간 (사용자가 메모 작성을 시작한 시간)
     * - 타임라인 정렬의 기준으로 사용됨
     * - 클라이언트에서 전송하거나 서버에서 자동 생성 가능
     * - 과거 시간도 설정 가능 (과거의 오늘의 흐름 기록용)
     */
    @Column(name = "memo_start_time", nullable = false)
    private LocalDateTime memoStartTime;
    
    /**
     * DB 레코드 생성 시간 (기술적 메타데이터)
     * - 서버에서 자동으로 설정 (DEFAULT CURRENT_TIMESTAMP)
     * - 언제 DB에 저장되었는지를 추적하는 감사(audit) 목적
     * - 메모 수정 시 변경되지 않음
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Memo() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public UserShelfBook getUserShelfBook() { return userShelfBook; }
    public void setUserShelfBook(UserShelfBook userShelfBook) { 
        this.userShelfBook = userShelfBook; 
    }
    
    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
    
    public LocalDateTime getMemoStartTime() { return memoStartTime; }
    public void setMemoStartTime(LocalDateTime memoStartTime) { 
        this.memoStartTime = memoStartTime; 
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public Long getUserId() { return user != null ? user.getId() : null; }
    public Long getUserShelfBookId() { 
        return userShelfBook != null ? userShelfBook.getId() : null; 
    }
}

