package com.readingtracker.server.service;

import com.readingtracker.dbms.entity.AladinBook;
import com.readingtracker.server.common.constant.BookSearchFilter;
import com.readingtracker.server.dto.responseDTO.AladinBookResponseDTO;
import com.readingtracker.server.mapper.AladinBookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 책 검색 관련 비즈니스 로직 및 흐름 제어를 담당하는 Service
 * 외부 API 통신은 AladinApiService에 위임하고, 비즈니스 로직만 처리합니다.
 */
@Service
public class BookSearchService {
    
    @Autowired
    private AladinApiService aladinApiService;
    
    @Autowired
    private AladinBookMapper aladinBookMapper;
    
    /**
     * 책 검색 (비즈니스 로직 포함)
     * 
     * @param query 검색어
     * @param queryType 검색 필터 (TITLE, AUTHOR, PUBLISHER)
     * @param start 시작 페이지
     * @param maxResults 페이지당 결과 수
     * @return 검색 결과 Entity 리스트
     */
    public List<AladinBook> searchBooks(String query, BookSearchFilter queryType, Integer start, Integer maxResults) {
        // 1. AladinApiService 호출하여 외부 DTO 획득
        AladinBookResponseDTO externalDto = aladinApiService.searchBooks(query, queryType, start, maxResults);
        
        // 2. Mapper를 통해 외부 DTO → 내부 Entity 변환
        List<AladinBook> entities = aladinBookMapper.toAladinBookList(externalDto);
        
        // 3. 검색 결과 검증 및 정제 (비즈니스 로직)
        // 알라딘 API의 자동 대체 방지를 위한 필터링
        List<AladinBook> filteredEntities = filterBooksBySearchCriteria(entities, query, queryType);
        
        // 4. Entity 리스트 반환
        return filteredEntities;
    }
    
    /**
     * 검색 기준에 따른 결과 검증 및 필터링
     * 알라딘 API가 검색어와 정확히 일치하지 않는 결과도 반환할 수 있으므로,
     * 실제로 검색 기준에 맞는 결과만 필터링합니다.
     */
    private List<AladinBook> filterBooksBySearchCriteria(List<AladinBook> books, String query, BookSearchFilter searchFilter) {
        if (books == null || books.isEmpty()) {
            return books;
        }
        
        String queryNormalized = query.replaceAll("\\s+", "").toLowerCase();
        
        return books.stream()
                .filter(book -> {
                    switch (searchFilter) {
                        case TITLE:
                            String title = book.getTitle();
                            if (title == null || title.trim().isEmpty()) {
                                return false;
                            }
                            String titleNormalized = title.replaceAll("\\s+", "").toLowerCase();
                            return titleNormalized.contains(queryNormalized);
                            
                        case AUTHOR:
                            String author = book.getAuthor();
                            if (author == null || author.trim().isEmpty()) {
                                return false;
                            }
                            String authorNormalized = author.replaceAll("\\s+", "").toLowerCase();
                            return authorNormalized.contains(queryNormalized);
                            
                        case PUBLISHER:
                            String publisher = book.getPublisher();
                            if (publisher == null || publisher.trim().isEmpty()) {
                                return false;
                            }
                            String publisherNormalized = publisher.replaceAll("\\s+", "").toLowerCase();
                            return publisherNormalized.contains(queryNormalized);
                            
                        default:
                            return true;  // 필터링 없음
                    }
                })
                .collect(Collectors.toList());
    }
}

