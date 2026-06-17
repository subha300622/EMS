package com.example.ems.support.dto;

import java.util.List;

public class FAQSearchResponse {
    private List<FAQArticleDto> articles;

    public FAQSearchResponse() {}

    public FAQSearchResponse(List<FAQArticleDto> articles) {
        this.articles = articles;
    }

    public List<FAQArticleDto> getArticles() { return articles; }
    public void setArticles(List<FAQArticleDto> articles) { this.articles = articles; }

    public static class FAQArticleDto {
        private Long articleId;
        private String title;
        private String content;
        private String category;
        private int views;
        private double helpfulnessScore; // e.g. 92.0

        public FAQArticleDto() {}

        public FAQArticleDto(Long articleId, String title, String content, String category, int views, double helpfulnessScore) {
            this.articleId = articleId;
            this.title = title;
            this.content = content;
            this.category = category;
            this.views = views;
            this.helpfulnessScore = helpfulnessScore;
        }

        public Long getArticleId() { return articleId; }
        public void setArticleId(Long articleId) { this.articleId = articleId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public int getViews() { return views; }
        public void setViews(int views) { this.views = views; }

        public double getHelpfulnessScore() { return helpfulnessScore; }
        public void setHelpfulnessScore(double helpfulnessScore) { this.helpfulnessScore = helpfulnessScore; }
    }
}
