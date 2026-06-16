package com.example.ems.performance.dto;

import java.util.List;

public class MyGoalListResponse {
    private List<MyGoalItem> goals;
    private long totalElements;
    private int totalPages;
    private int currentPage;

    public MyGoalListResponse(List<MyGoalItem> goals, long totalElements, int totalPages, int currentPage) {
        this.goals = goals;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    // Getters and Setters
    public List<MyGoalItem> getGoals() { return goals; }
    public void setGoals(List<MyGoalItem> goals) { this.goals = goals; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
}
