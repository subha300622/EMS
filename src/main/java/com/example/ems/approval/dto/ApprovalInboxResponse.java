package com.example.ems.approval.dto;

import java.util.List;

public class ApprovalInboxResponse {

    private List<ApprovalTaskDto> content;
    private long totalElements;
    private int page;
    private int size;

    public ApprovalInboxResponse() {}

    public ApprovalInboxResponse(List<ApprovalTaskDto> content, long totalElements, int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.page = page;
        this.size = size;
    }

    public List<ApprovalTaskDto> getContent() { return content; }
    public List<ApprovalTaskDto> getTasks() { return content; }
    public void setContent(List<ApprovalTaskDto> content) { this.content = content; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
