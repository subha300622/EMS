package com.example.ems.holiday.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated list response of organization holidays")
public class HolidayListResponse {

    @Schema(description = "List of holidays on current page")
    private List<HolidayResponseDto> content;

    @Schema(description = "Current page index (0-based)", example = "0")
    private int page;

    @Schema(description = "Number of items per page", example = "20")
    private int size;

    @Schema(description = "Total number of holidays matching filter", example = "12")
    private long totalElements;

    @Schema(description = "Total pages available", example = "1")
    private int totalPages;

    public HolidayListResponse() {}

    public HolidayListResponse(List<HolidayResponseDto> content, int page, int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<HolidayResponseDto> getContent() {
        return content;
    }

    public void setContent(List<HolidayResponseDto> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
