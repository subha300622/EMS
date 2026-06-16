package com.example.ems.asset.repository;

import com.example.ems.asset.entity.MyAssetIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MyAssetIssueRepository extends JpaRepository<MyAssetIssue, Long> {
    List<MyAssetIssue> findByEmployeeIdOrderByReportedAtDesc(Long employeeId);
}
