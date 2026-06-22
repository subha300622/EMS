package com.example.ems.employee.repository;

import com.example.ems.employee.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByActiveTrueOrderByPublishedDateDesc();
    Page<Announcement> findByActiveTrue(Pageable pageable);
}
