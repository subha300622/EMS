package com.example.ems.common.dto.manager;

public record AnnouncementCommentDto(
    Long id,
    Long announcementId,
    String content,
    String authorName,
    String createdAt
) {}
