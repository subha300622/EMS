package com.example.ems.common.dto.manager;

public record AnnouncementDto(
    Long id,
    String title,
    String description,
    String category,
    String postedBy,
    String postedDate,
    Integer likes,
    Integer comments,
    Integer views
) {}
