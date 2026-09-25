package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(description = "Team training progress list response")
public class TeamProgressListResponse {

    @Schema(description = "Team members training progress items")
    private List<Map<String, Object>> content;

    public TeamProgressListResponse() {}

    public List<Map<String, Object>> getContent() { return content; }
    public void setContent(List<Map<String, Object>> content) { this.content = content; }
}
