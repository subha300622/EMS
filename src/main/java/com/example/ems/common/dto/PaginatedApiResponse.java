package com.example.ems.common.dto;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class PaginatedApiResponse<T> extends ApiResponse<T> {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PaginatedApiResponse() {}

    public PaginatedApiResponse(boolean success, String message, String timestamp, T data, int page, int size, long totalElements, int totalPages) {
        super(success, message, timestamp, data);
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static <T> PaginatedApiResponse<T> success(String message, T data, int page, int size, long totalElements, int totalPages) {
        return new PaginatedApiResponse<>(
                true,
                message,
                Instant.now().truncatedTo(ChronoUnit.SECONDS).toString(),
                data,
                page,
                size,
                totalElements,
                totalPages
        );
    }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
