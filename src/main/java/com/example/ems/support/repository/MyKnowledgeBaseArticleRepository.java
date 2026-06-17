package com.example.ems.support.repository;

import com.example.ems.support.entity.MyKnowledgeBaseArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MyKnowledgeBaseArticleRepository extends JpaRepository<MyKnowledgeBaseArticle, Long> {
    List<MyKnowledgeBaseArticle> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword, String contentKeyword);
    List<MyKnowledgeBaseArticle> findByCategoryIgnoreCase(String category);
}
