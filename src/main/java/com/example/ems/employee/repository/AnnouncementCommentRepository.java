package com.example.ems.employee.repository;

import com.example.ems.employee.entity.AnnouncementComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementCommentRepository extends JpaRepository<AnnouncementComment, Long> {
    List<AnnouncementComment> findByAnnouncementIdOrderByCreatedAtDesc(Long announcementId);
    int countByAnnouncementId(Long announcementId);
}
