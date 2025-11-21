package com.readingtracker.server.mapper;

import com.readingtracker.dbms.entity.AladinBook;
import com.readingtracker.server.dto.responseDTO.AladinBookResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 알라딘 API 외부 DTO를 내부 Entity로 변환하는 Mapper
 * 알라딘 API의 필드명을 우리 서비스의 표준 필드명으로 변환합니다.
 */
@Mapper(componentModel = "spring")
public interface AladinBookMapper {
    
    AladinBookMapper INSTANCE = Mappers.getMapper(AladinBookMapper.class);
    
    /**
     * AladinBookResponseDTO → List<AladinBook> 변환
     */
    default List<AladinBook> toAladinBookList(AladinBookResponseDTO dto) {
        if (dto == null || dto.getItems() == null) {
            return List.of();
        }
        
        return dto.getItems().stream()
                .map(this::toAladinBook)
                .toList();
    }
    
    /**
     * AladinBookItemDTO → AladinBook 변환
     * 알라딘 API의 필드명을 우리 서비스의 표준 필드명으로 변환합니다.
     */
    @Mapping(target = "coverUrl", source = "cover")  // cover → coverUrl
    @Mapping(target = "priceSales", source = "pricesales")  // pricesales → priceSales
    @Mapping(target = "priceStandard", source = "pricestandard")  // pricestandard → priceStandard
    @Mapping(target = "publishedAt", expression = "java(parsePubDate(item.getPubdate()))")  // pubdate 문자열 → LocalDate
    @Mapping(target = "totalPages", expression = "java(parseTotalPages(item.getSubInfo()))")  // subInfo에서 추출
    @Mapping(target = "mainGenre", expression = "java(parseMainGenre(item.getSubInfo()))")  // subInfo에서 추출
    AladinBook toAladinBook(AladinBookResponseDTO.AladinBookItemDTO item);
    
    /**
     * pubdate 문자열을 LocalDate로 변환
     * 알라딘 API 형식: "yyyy-MM-dd"
     */
    default LocalDate parsePubDate(String pubdate) {
        if (pubdate == null || pubdate.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(pubdate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            // 날짜 파싱 실패 시 null 반환
            return null;
        }
    }
    
    /**
     * subInfo에서 총 페이지 수 추출
     * 알라딘 API의 subInfo.itemPage 또는 subInfo.itemPageCount 필드 사용
     */
    default Integer parseTotalPages(Map<String, Object> subInfo) {
        if (subInfo == null || subInfo.isEmpty()) {
            return null;
        }
        
        // 1순위: itemPage 필드
        if (subInfo.containsKey("itemPage")) {
            Object value = subInfo.get("itemPage");
            Integer intValue = parseInteger(value);
            if (intValue != null) {
                return intValue;
            }
        }
        
        // 2순위: itemPageCount 필드
        if (subInfo.containsKey("itemPageCount")) {
            Object value = subInfo.get("itemPageCount");
            Integer intValue = parseInteger(value);
            if (intValue != null) {
                return intValue;
            }
        }
        
        return null;
    }
    
    /**
     * subInfo에서 메인 장르 추출
     * 알라딘 API의 subInfo.categoryName 또는 categoryList에서 추출
     */
    default String parseMainGenre(Map<String, Object> subInfo) {
        if (subInfo == null || subInfo.isEmpty()) {
            return null;
        }
        
        // 1순위: categoryName 필드
        if (subInfo.containsKey("categoryName")) {
            Object value = subInfo.get("categoryName");
            if (value != null) {
                return value.toString();
            }
        }
        
        // 2순위: name 필드
        if (subInfo.containsKey("name")) {
            Object value = subInfo.get("name");
            if (value != null) {
                return value.toString();
            }
        }
        
        // 3순위: categoryList에서 첫 번째 카테고리의 categoryName 추출
        if (subInfo.containsKey("categoryList")) {
            Object categoryListObj = subInfo.get("categoryList");
            if (categoryListObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> categoryList = (List<Map<String, Object>>) categoryListObj;
                if (!categoryList.isEmpty()) {
                    Map<String, Object> firstCategory = categoryList.get(0);
                    // categoryName 우선
                    if (firstCategory.containsKey("categoryName")) {
                        Object value = firstCategory.get("categoryName");
                        if (value != null) {
                            return value.toString();
                        }
                    }
                    // name 폴백
                    if (firstCategory.containsKey("name")) {
                        Object value = firstCategory.get("name");
                        if (value != null) {
                            return value.toString();
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Object를 Integer로 변환하는 헬퍼 메서드
     */
    default Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}

