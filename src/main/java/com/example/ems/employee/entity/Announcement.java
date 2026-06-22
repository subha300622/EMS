package com.example.ems.employee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    private String author = "HR Department";

    @Column(nullable = false)
    private LocalDateTime publishedDate = LocalDateTime.now();

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, columnDefinition = "varchar(50) default 'GENERAL'")
    private String category = "GENERAL";

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int likes = 0;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int views = 0;

    public Announcement() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDateTime publishedDate) { this.publishedDate = publishedDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<AnnouncementComment> comments = new java.util.ArrayList<>();

    public java.util.List<AnnouncementComment> getComments() { return comments; }
    public void setComments(java.util.List<AnnouncementComment> comments) { this.comments = comments; }
}
