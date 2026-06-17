package com.example.ems.support.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "my_knowledge_base_articles")
public class MyKnowledgeBaseArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String category;

    private Integer views = 0;

    private Integer helpfulCount = 0;

    private Integer notHelpfulCount = 0;

    public MyKnowledgeBaseArticle() {}

    public MyKnowledgeBaseArticle(Long articleId, String title, String content, String category, Integer views, Integer helpfulCount, Integer notHelpfulCount) {
        this.articleId = articleId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.views = views;
        this.helpfulCount = helpfulCount;
        this.notHelpfulCount = notHelpfulCount;
    }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }

    public Integer getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(Integer helpfulCount) { this.helpfulCount = helpfulCount; }

    public Integer getNotHelpfulCount() { return notHelpfulCount; }
    public void setNotHelpfulCount(Integer notHelpfulCount) { this.notHelpfulCount = notHelpfulCount; }
}
