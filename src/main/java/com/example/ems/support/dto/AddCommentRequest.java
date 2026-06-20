package com.example.ems.support.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class AddCommentRequest {
    @Schema(example = "Excellent progress")
    private String commentText;
    private List<String> attachments; // list of fileIds

    public AddCommentRequest() {}

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
}
