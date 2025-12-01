package com.readingtracker.server.mapper;

import com.readingtracker.dbms.entity.AladinBook;
import com.readingtracker.dbms.entity.Book;
import com.readingtracker.dbms.entity.User;
import com.readingtracker.dbms.entity.UserShelfBook;
import com.readingtracker.server.common.constant.BookSearchFilter;
import com.readingtracker.server.dto.requestDTO.BookAdditionRequest;
import com.readingtracker.server.dto.requestDTO.BookDetailUpdateRequest;
import com.readingtracker.server.dto.requestDTO.FinishReadingRequest;
import com.readingtracker.server.dto.requestDTO.StartReadingRequest;
import com.readingtracker.server.dto.responseDTO.BookAdditionResponse;
import com.readingtracker.server.dto.responseDTO.BookSearchResponse;
import com.readingtracker.server.dto.responseDTO.MyShelfResponse;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {
    
    /**
     * BookAdditionRequest → Book Entity 변환
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userBooks", ignore = true)
    Book toBookEntity(BookAdditionRequest request);
    
    /**
     * BookAdditionRequest + User → UserShelfBook Entity 변환
     * 주의: Book은 별도로 생성되어야 함
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "book", ignore = true) // Service에서 별도 설정 필요
    @Mapping(target = "category", source = "request.category")
    @Mapping(target = "categoryManuallySet", constant = "true")
    @Mapping(target = "expectation", source = "request.expectation")
    @Mapping(target = "readingStartDate", source = "request.readingStartDate")
    @Mapping(target = "readingProgress", source = "request.readingProgress")
    @Mapping(target = "purchaseType", source = "request.purchaseType")
    @Mapping(target = "readingFinishedDate", source = "request.readingFinishedDate")
    @Mapping(target = "rating", source = "request.rating")
    @Mapping(target = "review", source = "request.review")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserShelfBook toUserShelfBookEntity(BookAdditionRequest request, User user);
    
    /**
     * UserShelfBook → BookAdditionResponse 변환
     */
    @Mapping(target = "message", constant = "책이 내 서재에 추가되었습니다.")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "title", source = "book.title")
    @Mapping(target = "category", source = "category")
    BookAdditionResponse toBookAdditionResponse(UserShelfBook userBook);
    
    /**
     * UserShelfBook → MyShelfResponse.ShelfBook 변환
     */
    @Mapping(target = "userBookId", source = "id")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "isbn", source = "book.isbn")
    @Mapping(target = "title", source = "book.title")
    @Mapping(target = "author", source = "book.author")
    @Mapping(target = "publisher", source = "book.publisher")
    @Mapping(target = "description", source = "book.description")
    @Mapping(target = "coverUrl", source = "book.coverUrl")
    @Mapping(target = "totalPages", source = "book.totalPages")
    @Mapping(target = "mainGenre", source = "book.mainGenre")
    @Mapping(target = "pubDate", source = "book.pubDate")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "expectation", source = "expectation")
    @Mapping(target = "lastReadPage", source = "readingProgress")
    @Mapping(target = "lastReadAt", source = "readingStartDate")
    @Mapping(target = "readingFinishedDate", source = "readingFinishedDate")
    @Mapping(target = "purchaseType", source = "purchaseType")
    @Mapping(target = "addedAt", source = "createdAt")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "review", source = "review")
    MyShelfResponse.ShelfBook toShelfBook(UserShelfBook userBook);
    
    /**
     * List<UserShelfBook> → MyShelfResponse 변환
     */
    default MyShelfResponse toMyShelfResponse(List<UserShelfBook> userBooks) {
        if (userBooks == null || userBooks.isEmpty()) {
            return new MyShelfResponse(List.of(), 0);
        }
        
        List<MyShelfResponse.ShelfBook> shelfBooks = userBooks.stream()
            .filter(ub -> ub != null && ub.getBook() != null) // null 체크 추가
            .map(this::toShelfBook)
            .toList();
        
        return new MyShelfResponse(shelfBooks, shelfBooks.size());
    }
    
    /**
     * AladinBook → BookSearchResponse.BookInfo 변환
     */
    @Mapping(target = "isbn", source = "isbn")
    @Mapping(target = "isbn13", source = "isbn13")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "publisher", source = "publisher")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "coverUrl", source = "coverUrl")
    @Mapping(target = "totalPages", source = "totalPages")
    @Mapping(target = "mainGenre", source = "mainGenre")
    @Mapping(target = "pubDate", source = "publishedAt")  // publishedAt → pubDate
    @Mapping(target = "priceSales", source = "priceSales")
    @Mapping(target = "priceStandard", source = "priceStandard")
    BookSearchResponse.BookInfo toBookSearchBookInfo(AladinBook aladinBook);
    
    /**
     * List<AladinBook> → BookSearchResponse 변환
     */
    default BookSearchResponse toBookSearchResponse(List<AladinBook> books, String query, BookSearchFilter searchFilter) {
        if (books == null) {
            return new BookSearchResponse(List.of(), 0, 1, 10, query, searchFilter);
        }
        
        List<BookSearchResponse.BookInfo> bookInfos = books.stream()
            .map(this::toBookSearchBookInfo)
            .toList();
        
        return new BookSearchResponse(bookInfos, bookInfos.size(), 1, bookInfos.size(), query, searchFilter);
    }
    
    /**
     * StartReadingRequest → UserShelfBook Entity 업데이트
     * 기존 Entity에 DTO의 필드값을 설정합니다.
     */
    default void updateUserShelfBookFromStartReadingRequest(UserShelfBook userBook, StartReadingRequest request) {
        if (request == null) {
            return;
        }
        
        if (request.getReadingStartDate() != null) {
            userBook.setReadingStartDate(request.getReadingStartDate());
        }
        
        if (request.getReadingProgress() != null) {
            userBook.setReadingProgress(request.getReadingProgress());
        }
        
        if (request.getPurchaseType() != null) {
            userBook.setPurchaseType(request.getPurchaseType());
        }
    }
    
    /**
     * FinishReadingRequest → UserShelfBook Entity 업데이트
     * 기존 Entity에 DTO의 필드값을 설정합니다.
     */
    default void updateUserShelfBookFromFinishReadingRequest(UserShelfBook userBook, FinishReadingRequest request) {
        if (request == null) {
            return;
        }
        
        if (request.getReadingFinishedDate() != null) {
            userBook.setReadingFinishedDate(request.getReadingFinishedDate());
        }
        
        if (request.getRating() != null) {
            userBook.setRating(request.getRating());
        }
        
        if (request.getReview() != null) {
            userBook.setReview(request.getReview());
        }
    }
    
    /**
     * BookDetailUpdateRequest → UserShelfBook Entity 업데이트
     * 기존 Entity에 DTO의 필드값을 설정합니다. (null이 아닌 값만 업데이트)
     */
    default void updateUserShelfBookFromBookDetailUpdateRequest(UserShelfBook userBook, BookDetailUpdateRequest request) {
        if (request == null) {
            return;
        }
        
        if (request.getCategory() != null) {
            userBook.setCategory(request.getCategory());
            userBook.setCategoryManuallySet(true);
        }
        
        if (request.getExpectation() != null) {
            userBook.setExpectation(request.getExpectation());
        }
        
        if (request.getReadingStartDate() != null) {
            userBook.setReadingStartDate(request.getReadingStartDate());
        }
        
        if (request.getReadingProgress() != null) {
            userBook.setReadingProgress(request.getReadingProgress());
        }
        
        if (request.getPurchaseType() != null) {
            userBook.setPurchaseType(request.getPurchaseType());
        }
        
        if (request.getReadingFinishedDate() != null) {
            userBook.setReadingFinishedDate(request.getReadingFinishedDate());
        }
        
        if (request.getRating() != null) {
            userBook.setRating(request.getRating());
        }
        
        if (request.getReview() != null) {
            userBook.setReview(request.getReview());
        }
    }
}

