package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Platform Support Categories Paginated Response")
public class PlatformCategoryPageResponse {

    @Schema(description = "List of category items")
    private List<PlatformCategoryListItemResponse> content;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int page;

    @Schema(description = "Page size", example = "20")
    private int size;

    @Schema(description = "Total number of categories", example = "2")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "1")
    private int totalPages;

    public PlatformCategoryPageResponse() {}

    public PlatformCategoryPageResponse(List<PlatformCategoryListItemResponse> content,
                                        int page, int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<PlatformCategoryListItemResponse> getContent() { return content; }
    public void setContent(List<PlatformCategoryListItemResponse> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int totalPages() { return totalPages; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
