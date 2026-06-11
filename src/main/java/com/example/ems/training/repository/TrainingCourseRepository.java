package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingCourse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingCourseRepository extends JpaRepository<TrainingCourse, Long> {
    List<TrainingCourse> findByStatus(String status);
}
