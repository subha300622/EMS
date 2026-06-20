package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class AppraisalHistoryResponse {
    private List<HistoryItem> history;

    public static class HistoryItem {
        @Schema(example = "string")
        private String year;
        @Schema(example = "string")
        private String cycleName;
        @Schema(example = "1")
        private Integer rating;
        @Schema(example = "string")
        private String incrementPercentage;
        @Schema(example = "string")
        private String newCtc;

        public HistoryItem(String year, String cycleName, Integer rating, String incrementPercentage, String newCtc) {
            this.year = year;
            this.cycleName = cycleName;
            this.rating = rating;
            this.incrementPercentage = incrementPercentage;
            this.newCtc = newCtc;
        }

        // Getters and Setters
        public String getYear() { return year; }
        public void setYear(String year) { this.year = year; }
        public String getCycleName() { return cycleName; }
        public void setCycleName(String cycleName) { this.cycleName = cycleName; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getIncrementPercentage() { return incrementPercentage; }
        public void setIncrementPercentage(String incrementPercentage) { this.incrementPercentage = incrementPercentage; }
        public String getNewCtc() { return newCtc; }
        public void setNewCtc(String newCtc) { this.newCtc = newCtc; }
    }

    public List<HistoryItem> getHistory() { return history; }
    public void setHistory(List<HistoryItem> history) { this.history = history; }
}
