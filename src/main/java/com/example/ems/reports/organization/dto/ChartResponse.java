package com.example.ems.reports.organization.dto;

import java.util.List;

public class ChartResponse {
    private List<String> labels;
    private List<Number> values;

    public ChartResponse() {}

    public ChartResponse(List<String> labels, List<Number> values) {
        this.labels = labels;
        this.values = values;
    }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }

    public List<Number> getValues() { return values; }
    public void setValues(List<Number> values) { this.values = values; }
}
