package com.example.ems.auth.dto;

import java.util.List;

public class TemplateDiffResponse {
    private List<String> added;
    private List<String> removed;

    public TemplateDiffResponse() {}

    public TemplateDiffResponse(List<String> added, List<String> removed) {
        this.added = added;
        this.removed = removed;
    }

    public List<String> getAdded() { return added; }
    public void setAdded(List<String> added) { this.added = added; }

    public List<String> getRemoved() { return removed; }
    public void setRemoved(List<String> removed) { this.removed = removed; }
}
