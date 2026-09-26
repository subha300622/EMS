package com.example.ems.reports.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Chart series data point response")
public class ChartResponse {
    @Schema(description = "Time or category labels", example = "[\"Jan\", \"Feb\", \"Mar\"]")
    private List<String> labels;

    @Schema(description = "Series data values", example = "[10, 25, 40]")
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
