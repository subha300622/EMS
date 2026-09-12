package com.example.ems.training.dto;

import com.example.ems.training.entity.DeliveryMethod;
import com.example.ems.training.entity.TrainingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Training Calendar Event Response")
public class CalendarEventResponse {
    @Schema(description = "Training ID", example = "1")
    private Long trainingId;
    @Schema(description = "Training Title", example = "Security Awareness Workshop")
    private String title;
    @Schema(description = "Session start datetime")
    private LocalDateTime start;
    @Schema(description = "Session end datetime")
    private LocalDateTime end;
    @Schema(description = "Delivery method", example = "ONLINE")
    private DeliveryMethod deliveryMethod;
    @Schema(description = "Training status", example = "SCHEDULED")
    private TrainingStatus status;
    @Schema(description = "Category", example = "Security")
    private String category;
    @Schema(description = "Trainer ID", example = "5")
    private Long trainerId;

    public Long getTrainingId() { return trainingId; }
    public void setTrainingId(Long trainingId) { this.trainingId = trainingId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }

    public DeliveryMethod getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(DeliveryMethod deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public TrainingStatus getStatus() { return status; }
    public void setStatus(TrainingStatus status) { this.status = status; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }
}
