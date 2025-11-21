package com.readingtracker.server.controller.v1;

import com.readingtracker.server.common.constant.BookSearchFilter;
import com.readingtracker.server.dto.ApiResponse;
import com.readingtracker.server.dto.responseDTO.BookDetailResponse;
import com.readingtracker.server.dto.responseDTO.BookSearchResponse;
import com.readingtracker.server.mapper.BookMapper;
import com.readingtracker.server.service.AladinApiService;
import com.readingtracker.server.service.BookSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "책 검색", description = "책 검색 및 도서 세부 정보 조회 API")
public class BookSearchController extends BaseV1Controller {
    
    @Autowired
    private BookSearchService bookSearchService;
    
    @Autowired
    private BookMapper bookMapper;
    
    @Autowired
    private AladinApiService aladinApiService;  // getBookDetail 메서드용
    
    /**
     * 책 검색 (비인증)
     * GET /api/v1/books/search
     */
    @GetMapping("/books/search")
    @Operation(
        summary = "책 검색", 
        description = "알라딘 Open API를 통해 책을 검색합니다 (비인증 접근 가능)"
    )
    public ApiResponse<BookSearchResponse> searchBooks(
            @Parameter(description = "검색어", required = true)
            @RequestParam String query,
            @Parameter(description = "검색 필터 (TITLE: 도서명, AUTHOR: 저자명, PUBLISHER: 출판사명)")
            @RequestParam(defaultValue = "TITLE") BookSearchFilter queryType,
            @Parameter(description = "시작 페이지")
            @RequestParam(defaultValue = "1") Integer start,
            @Parameter(description = "페이지당 결과 수 (최대 50)")
            @RequestParam(defaultValue = "10") Integer maxResults) {
        
        // BookSearchService 호출 (개별 파라미터 전달)
        var entities = bookSearchService.searchBooks(query, queryType, start, maxResults);
        
        // Entity → ResponseDTO 변환 (Mapper 사용)
        BookSearchResponse response = bookMapper.toBookSearchResponse(entities, query, queryType);
        
        return ApiResponse.success(response);
    }
    
    /**
     * 도서 세부 정보 검색 (비인증)
     * GET /api/v1/books/{isbn}
     */
    @GetMapping("/books/{isbn}")
    @Operation(
        summary = "도서 세부 정보 검색",
        description = "ISBN을 통해 알라딘 Open API에서 도서의 상세 정보를 조회합니다 (비인증 접근 가능)"
    )
    public ApiResponse<BookDetailResponse> getBookDetail(
            @Parameter(description = "도서 ISBN", required = true)
            @PathVariable String isbn) {
        
        BookDetailResponse response = aladinApiService.getBookDetail(isbn);
        
        return ApiResponse.success(response);
    }
}

